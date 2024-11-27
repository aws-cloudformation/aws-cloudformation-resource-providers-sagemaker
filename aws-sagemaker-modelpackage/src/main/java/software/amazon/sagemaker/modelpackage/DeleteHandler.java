package software.amazon.sagemaker.modelpackage;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
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
                proxy.initiate("AWS-SageMaker-ModelPackage::Delete", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall(this::deleteModelPackageResource)
                    .stabilize(this::stabilizedOnDelete)
                    .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .build()));
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     * @param deleteModelPackageRequest the aws service request to delete a model package
     * @param proxyClient the aws service client to make the call
     * @return delete model package response
     */
    private DeleteModelPackageResponse deleteModelPackageResource(
        final DeleteModelPackageRequest deleteModelPackageRequest,
        final ProxyClient<SageMakerClient> proxyClient) {
        DeleteModelPackageResponse response = null;
        logger.log(String.format("Updating model package with %s", deleteModelPackageRequest.toString()));

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(deleteModelPackageRequest, proxyClient.client()::deleteModelPackage);
        } catch (ResourceNotFoundException e) {
            // NotFound responded from Delete handler will be considered as success by CFN backend service.
            // This is to handle out of stack resource deletion
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteModelPackageRequest.modelPackageName());
        }  catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME, deleteModelPackageRequest.modelPackageName(), e);
        }
        return response;
    }

    /**
     * Sync delete API moves resource in PENDING state and actual deletion happens asynchronously.
     * Stabilization is required to ensure model package resource deletion has been completed.
     * @param deleteModelPackageRequest the aws service request to delete a model package
     * @param deleteModelPackageResult the aws service response on deleting a model package
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnDelete(
        final DeleteModelPackageRequest deleteModelPackageRequest,
        final DeleteModelPackageResponse deleteModelPackageResult,
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext) {
        try {
            DescribeModelPackageResponse response = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                proxyClient.client()::describeModelPackage);
            final ModelPackageStatus modelPackageStatus = response.modelPackageStatus();
            logger.log(String.format("Deleting Model Package: %s. Stabilizing...", response.toString()));

            switch (modelPackageStatus) {
                case DELETING:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.", ResourceModel.TYPE_NAME, model.getModelPackageName()));
                    return false;
                default:
                    throw new CfnGeneralServiceException("Delete stabilizing of model package: " + model.getModelPackageName());
            }
        } catch (final ResourceNotFoundException e) {
            return true;
        } catch (final software.amazon.awssdk.services.sagemaker.model.SageMakerException e) {
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().matches(".* does not exist.*")) {
                return true;
            }
            throw e;
        }
    }

}
