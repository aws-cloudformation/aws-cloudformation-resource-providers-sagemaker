package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.UpdateAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateAppImageConfigResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-AppImageConfig::Update";
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
                                .done(awsResponse -> constructResourceModelFromResponse(model, awsResponse))
                );
    }

    /**
     * Client invocation of the update request through the proxyClient
     *
     * @param awsRequest aws service update resource request
     * @param proxyClient the aws service client to make the call
     * @return update resource response
     */
    private UpdateAppImageConfigResponse updateResource(
            final UpdateAppImageConfigRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient
    ) {
        UpdateAppImageConfigResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::updateAppImageConfig);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, awsRequest.appImageConfigName(), e);
        }
        return response;
    }

    /**
     * Build the Progress Event object from the update response.
     *
     * @param model resource model
     * @param awsResponse aws service update resource response
     * @return progressEvent indicating success
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ResourceModel model, final UpdateAppImageConfigResponse awsResponse) {
        model.setAppImageConfigArn(awsResponse.appImageConfigArn());
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
