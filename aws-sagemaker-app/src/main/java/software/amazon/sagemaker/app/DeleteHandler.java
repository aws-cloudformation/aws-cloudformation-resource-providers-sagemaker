package software.amazon.sagemaker.app;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AppStatus;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-App::Delete";

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
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToDeleteRequest)
                                .makeServiceCall((_awsRequest, _proxyClient) ->
                                        deleteResource(TranslatorForRequest.translateToReadRequest(model), _awsRequest, _proxyClient))
                                .stabilize(this::stabilizedOnDelete)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Client invocation of the delete request through the proxyClient.
     *
     * @param deleteRequest the aws service delete resource request
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private DeleteAppResponse deleteResource(
            final DescribeAppRequest describeRequest,
            final DeleteAppRequest deleteRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        AppStatus appStatus = null;
        try {
            appStatus = proxyClient.injectCredentialsAndInvokeV2(describeRequest, proxyClient.client()::describeApp).status();
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    deleteRequest.appName(), e);
        }

        // Deleted Apps stay present for 24 hours with a Deleted status.
        // Deleted resources are expected to throw CfnNotFoundException.
        if (appStatus.equals(AppStatus.DELETED) || appStatus.equals(AppStatus.FAILED)) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteRequest.appName());
        }

        DeleteAppResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(deleteRequest, proxyClient.client()::deleteApp);
        } catch (final ResourceInUseException riue) {
            // ResourceInUseException is handled differently for deletes
            final String primaryIdentifier = String.format("%s|%s|%s|%s",
                    deleteRequest.appName(),
                    deleteRequest.appType(),
                    deleteRequest.domainId(),
                    deleteRequest.userProfileName());
            throw new CfnResourceConflictException(ResourceModel.TYPE_NAME, primaryIdentifier, riue.getMessage(), riue);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME,
                    deleteRequest.appName(), e);
        }

        return response;
    }

    /**
     * Ensure resource has moved from pending to terminal state.
     *
     * @param awsRequest the aws service delete resource request
     * @param awsResponse the aws service delete resource response
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     * @param callbackContext callback context
     * @return boolean to indicate if the deletion is stabilized
     */
    private boolean stabilizedOnDelete(
            final DeleteAppRequest awsRequest,
            final DeleteAppResponse awsResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            final AppStatus AppStatus =
                    proxyClient.injectCredentialsAndInvokeV2(TranslatorForRequest.translateToReadRequest(model),
                            proxyClient.client()::describeApp).status();

            switch (AppStatus) {
                case DELETED:
                    logger.log(String.format("%s with name [%s] is stabilized.",
                            ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                    return true;
                case DELETING:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.",
                            ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                    return false;
                default:
                    throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getAppName());
            }
        } catch (ResourceNotFoundException e) {
            return true;
        }
    }
}
