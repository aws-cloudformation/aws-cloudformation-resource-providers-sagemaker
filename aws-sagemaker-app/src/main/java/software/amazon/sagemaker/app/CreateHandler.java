package software.amazon.sagemaker.app;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AppStatus;
import software.amazon.awssdk.services.sagemaker.model.CreateAppRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateAppResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-App::Create";
    private static final String READ_ONLY_PROPERTY_ERROR_MESSAGE = "The following property '%s' is not allowed to configured.";

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        // read only properties are not allowed to be set by the user during creation.
        // https://github.com/aws-cloudformation/aws-cloudformation-resource-schema/issues/102
        if (callbackContext.callGraphs().isEmpty()) {
            if (model.getAppArn() != null) {
                throw new CfnInvalidRequestException(String.format(READ_ONLY_PROPERTY_ERROR_MESSAGE, "AppArn"));
            }
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToCreateRequest)
                                .makeServiceCall(this::createResource)
                                .stabilize(this::stabilizedOnCreate)
                                .done(createResponse -> constructResourceModelFromResponse(model, createResponse))
                );
    }

    /**
     * Client invocation of the create request through the proxyClient.
     *
     * @param createRequest aws service create resource request
     * @param proxyClient   aws service client to make the call
     * @return create resource response
     */
    private CreateAppResponse createResource(
            final CreateAppRequest createRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        CreateAppResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    createRequest, proxyClient.client()::createApp);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createRequest.appName(), e);
        }
        return response;
    }

    /**
     * Ensure resource has moved from pending to terminal state.
     *
     * @param awsRequest the aws service create resource request
     * @param awsResponse the aws service create response
     * @param proxyClient the aws service client to make the call
     * @param model Resource Model
     * @param callbackContext call back context
     * @return boolean to indicate if the creation is stabilized
     */
    private boolean stabilizedOnCreate(
            final CreateAppRequest awsRequest,
            final CreateAppResponse awsResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if (model.getAppName() == null) {
            model.setAppName(awsRequest.appName());
        }

        final AppStatus AppStatus;
        try {
            AppStatus = proxyClient.injectCredentialsAndInvokeV2(
                    TranslatorForRequest.translateToReadRequest(model),
                    proxyClient.client()::describeApp).status();
        } catch (ResourceNotFoundException rnfe) {
            logger.log(String.format("Resource not found for %s, stabilizing.", model.getPrimaryIdentifier()));
            return false;
        }

        switch (AppStatus) {
            case IN_SERVICE:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), AppStatus));
                return true;
            case PENDING:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier()));
                return false;
            default:
                logger.log(String.format("%s [%s] failed to stabilize with status: %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), AppStatus));
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getAppName());
        }
    }

    /**
     * Build the Progress Event object from the create response.
     *
     * @param model resource model
     * @param awsResponse aws service create resource response
     * @return progressEvent indicating success
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ResourceModel model, final CreateAppResponse awsResponse) {
        model.setAppArn(awsResponse.appArn());
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
