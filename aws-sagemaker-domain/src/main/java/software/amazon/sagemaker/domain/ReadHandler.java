package software.amazon.sagemaker.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Domain::Read";

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
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest, sdkProxyClient, model))
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the read request through the proxyClient.
     *
     * @param awsRequest the aws service describe resource request
     * @param proxyClient the aws service client to make the call
     * @param model Resource Model
     * @return describe resource response
     */
    private DescribeDomainResponse readResource(
            final DescribeDomainRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {

        DescribeDomainResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeDomain);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    awsRequest.domainId(), e);
        }

        return response;
    }

    /**
     * Build the Progress Event object from the SageMaker DescribeDomain response.
     *
     * @param awsResponse the aws service list resource response
     * @return progressEvent indicating either success, delay, callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeDomainResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(TranslatorForResponse.translateFromReadResponse(awsResponse));
    }
}
