package software.amazon.sagemaker.space;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.UpdateSpaceRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateSpaceResponse;
import software.amazon.awssdk.services.sagemaker.model.SpaceStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Space::Update";
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SageMakerClient> proxyClient,
            Logger logger
    ) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToUpdateRequest)
                                .makeServiceCall(this::updateResource)
                                .stabilize(this::stabilizedOnUpdate)
                                .done(updateResponse -> constructResourceModelFromResponse(model, updateResponse))
                );
    }

    /**
     * Client invocation of the update request through the proxyClient.
     *
     * @param awsRequest the aws service update resource request
     * @param proxyClient the aws service client to make the call
     * @return aws service update resource response
     */
    private UpdateSpaceResponse updateResource(
            final UpdateSpaceRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient
    ) {
        UpdateSpaceResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::updateSpace);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, awsRequest.spaceName(), e);
        }
        return response;
    }

    /**
     * Ensure resource has moved from pending to terminal state.
     *
     * @param updateSpaceRequest the aws service update resource request
     * @param proxyClient the aws service client to make the call
     * @return boolean indicating if the resource is stabilized
     */
    private boolean stabilizedOnUpdate(
            final UpdateSpaceRequest updateSpaceRequest,
            final UpdateSpaceResponse updateSpaceResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        final SpaceStatus spaceStatus = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeSpace).status();

        switch (spaceStatus) {
            case IN_SERVICE:
                logger.log(String.format("%s [%s] has been stabilized with state %s during update operation.",
                        ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), spaceStatus));
                return true;
            case PENDING:
            case UPDATING:
                logger.log(String.format("%s [%s] is stabilizing during update.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing during update of " + model.getPrimaryIdentifier());

        }
    }

    /**
     * Build the Progress Event object from the update response.
     *
     * @param model resource model
     * @param awsResponse aws service update resource response
     * @return progressEvent indicating success
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ResourceModel model,
            final UpdateSpaceResponse awsResponse) {
        model.setSpaceArn(awsResponse.spaceArn());
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
