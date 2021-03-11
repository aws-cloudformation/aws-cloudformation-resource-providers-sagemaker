package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateAppImageConfigResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-AppImageConfig::Create";
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
        if (model.getAppImageConfigArn() != null) {
            throw new CfnInvalidRequestException(String.format(READ_ONLY_PROPERTY_ERROR_MESSAGE, "AppImageConfig"));
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToCreateRequest)
                                .makeServiceCall(this::createResource)
                                .done(createResponse -> constructResourceModelFromResponse(model, createResponse))
                );
    }

    /**
     * Client invocation of the create request through the proxyClient
     *
     * @param createRequest aws service create resource request
     * @param proxyClient   aws service client to make the call
     * @return create resource response
     */
    private CreateAppImageConfigResponse createResource(
            final CreateAppImageConfigRequest createRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        CreateAppImageConfigResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    createRequest, proxyClient.client()::createAppImageConfig);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createRequest.appImageConfigName(), e);
        }
        return response;
    }

    /**
     * Build the Progress Event object from the create response.
     *
     * @param model resource model
     * @param awsResponse aws service create resource response
     * @return progressEvent indicating success
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ResourceModel model, final CreateAppImageConfigResponse awsResponse) {
        model.setAppImageConfigArn(awsResponse.appImageConfigArn());
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
