package software.amazon.sagemaker.userprofile;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteUserProfileResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.UserProfileStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-UserProfile::Delete";

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
                                .makeServiceCall(this::deleteResource)
                                .stabilize(this::stabilizedOnDelete)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     *
     * @param awsRequest the aws service delete resource request
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private DeleteUserProfileResponse deleteResource(
            final DeleteUserProfileRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteUserProfileResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteUserProfile);
        } catch (final ResourceInUseException riue) {
            // ResourceInUseException is handled differently for deletes, as deletes can fail due to associated Apps
            final String primaryIdentifier = String.format("%s|%s", awsRequest.domainId(), awsRequest.userProfileName());
            throw new CfnResourceConflictException(ResourceModel.TYPE_NAME, primaryIdentifier, riue.getMessage(), riue);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME,
                    awsRequest.userProfileName(), e);
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
     * @return boolean to indicate if the creation is stabilized
     */
    private boolean stabilizedOnDelete(
            final DeleteUserProfileRequest awsRequest,
            final DeleteUserProfileResponse awsResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            final UserProfileStatus UserProfileStatus =
                    proxyClient.injectCredentialsAndInvokeV2(TranslatorForRequest.translateToReadRequest(model),
                            proxyClient.client()::describeUserProfile).status();

            switch (UserProfileStatus) {
                case DELETING:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.",
                            ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                    return false;
                default:
                    throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getUserProfileName());
            }
        } catch (ResourceNotFoundException e) {
            return true;
        }
    }
}
