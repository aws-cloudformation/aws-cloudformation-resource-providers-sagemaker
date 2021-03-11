package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppImageConfigResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-AppImageConfig::Read";

    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<SageMakerClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                .translateToServiceRequest(TranslatorForRequest::translateToReadRequest)
                .makeServiceCall(this::readResource)
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the read request through the proxyClient
     *
     * @param awsRequest aws describe resource request
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeAppImageConfigResponse readResource(
            final DescribeAppImageConfigRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DescribeAppImageConfigResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeAppImageConfig);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    awsRequest.appImageConfigName(), e);
        }

        return response;
    }

    /**
     * Build the Progress Event object from the read response.
     *
     * @param awsResponse aws service describe resource response
     * @return progressEvent indicating success
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeAppImageConfigResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(TranslatorForResponse.translateFromReadResponse(awsResponse));
    }
}
