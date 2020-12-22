package software.amazon.sagemaker.modelpackagegroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageGroupStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;

public class CreateHandler extends BaseHandlerStd {

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
                        proxy.initiate("AWS-SageMaker-ModelPackageGroup::Create", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::createResource)
                                .stabilize(this::stabilizedOnCreate)
                                .progress()
                )
                .then(progress -> putResourcePolicy(proxyClient, model, callbackContext))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param createModelPackageGroupRequest the aws service request to create a model package group
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create model package resource response
     */
    private CreateModelPackageGroupResponse createResource(
            final CreateModelPackageGroupRequest createModelPackageGroupRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        CreateModelPackageGroupResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    createModelPackageGroupRequest, proxyClient.client()::createModelPackageGroup);
        } catch (final ResourceInUseException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, createModelPackageGroupRequest.modelPackageGroupName());
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createModelPackageGroupRequest.modelPackageGroupName(), e);
        }
        return response;
    }

    /**
     * Client invocation of the put policy request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return progressEvent, in progress with delay callback and model state
     */
    private ProgressEvent<ResourceModel, CallbackContext> putResourcePolicy(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            if (model.getModelPackageGroupPolicy() != null) {
                proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToPutModelPackageGroupPolicyRequest(model), proxyClient.client()::putModelPackageGroupPolicy);
            }
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.CREATE.toString(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * This is used to ensure model package group resource has moved from Pending to Scheduled/Failed state.
     * @param createModelPackageGroupRequest the aws service request to create a model package group
     * @param createModelPackageGroupResponse the aws service response on creating a model package group
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnCreate(
            final CreateModelPackageGroupRequest createModelPackageGroupRequest,
            final CreateModelPackageGroupResponse createModelPackageGroupResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if(model.getModelPackageGroupArn() == null){
            model.setModelPackageGroupArn(createModelPackageGroupResponse.modelPackageGroupArn());
        }

        final DescribeModelPackageGroupResponse response = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                proxyClient.client()::describeModelPackageGroup);
        final ModelPackageGroupStatus modelPackageGroupStatus = response.modelPackageGroupStatus();

        switch (modelPackageGroupStatus) {
            case COMPLETED:
            case FAILED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), modelPackageGroupStatus));
                return true;
            case PENDING:
            case IN_PROGRESS:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException(
                        "Stabilizing of " + model.getPrimaryIdentifier()
                        + " failed with unexpected status " + modelPackageGroupStatus);
        }
    }
}
