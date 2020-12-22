package software.amazon.sagemaker.modelpackagegroup;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageGroupStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SageMaker-ModelPackageGroup::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .stabilize(this::stabilizedOnDelete)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     * @param deleteModelPackageGroupRequest the aws service request to delete a model package group
     * @param proxyClient the aws service client to make the call
     * @return delete model package group response
     */
    private DeleteModelPackageGroupResponse deleteResource(
            final DeleteModelPackageGroupRequest deleteModelPackageGroupRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteModelPackageGroupResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(deleteModelPackageGroupRequest, proxyClient.client()::deleteModelPackageGroup);
        } catch (ResourceNotFoundException e) {
            // NotFound responded from Delete handler will be considered as success by CFN backend service.
            // This is to handle out of stack resource deletion
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteModelPackageGroupRequest.modelPackageGroupName());
        }  catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME, deleteModelPackageGroupRequest.modelPackageGroupName(), e);
        }

        return response;
    }

    /**
     * Sync delete API moves resource in PENDING state and actual deletion happens asynchronously.
     * Stabilization is required to ensure model package group resource deletion has been completed.
     * @param deleteModelPackageGroupRequest the aws service request to delete a model package group
     * @param deleteModelPackageGroupResult the aws service response on deleting a model package group
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnDelete(
            final DeleteModelPackageGroupRequest deleteModelPackageGroupRequest,
            final DeleteModelPackageGroupResponse deleteModelPackageGroupResult,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
             DescribeModelPackageGroupResponse response = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                    proxyClient.client()::describeModelPackageGroup);
            final ModelPackageGroupStatus modelPackageGroupStatus = response.modelPackageGroupStatus();

            switch (modelPackageGroupStatus) {
                case DELETING:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.", ResourceModel.TYPE_NAME, model.getModelPackageGroupName()));
                    return false;
                default:
                    throw new CfnGeneralServiceException("Delete stabilizing of model package group: " + model.getModelPackageGroupName());
            }
        } catch (final ResourceNotFoundException e) {
            return true;
        } catch (final SageMakerException e) {
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().matches(".*ModelPackageGroup .* does not exist.*")) {
                return true;
            }
            throw e;
        }
    }
}
