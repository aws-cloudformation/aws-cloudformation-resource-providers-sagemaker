package software.amazon.sagemaker.featuregroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-FeatureGroup::Read";

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
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
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @param model Resource Model
     * @return describe resource response
     */
    private DescribeFeatureGroupResponse readResource(
            final DescribeFeatureGroupRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {

        DescribeFeatureGroupResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeFeatureGroup);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    awsRequest.featureGroupName(), e);
        }

        return response;
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already
     * initialised with caller credentials, correct region and retry settings
     *
     * @param awsResponse the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeFeatureGroupResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(TranslatorForResponse.translateFromReadResponse(awsResponse));
    }
}
