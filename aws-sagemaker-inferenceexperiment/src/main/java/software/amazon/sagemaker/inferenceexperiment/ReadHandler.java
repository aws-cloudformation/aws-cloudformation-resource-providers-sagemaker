package software.amazon.sagemaker.inferenceexperiment;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-SageMaker-InferenceExperiment::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(TranslatorForRequest::translateToReadRequest)
                .makeServiceCall(this::readResource)
                .done(response -> constructResourceModelFromResponse(response, proxyClient));
    }

    /**
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param request the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeInferenceExperimentResponse readResource(
            final DescribeInferenceExperimentRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        DescribeInferenceExperimentResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeInferenceExperiment);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.name(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already
     * initialised with caller credentials, correct region and retry settings
     *
     * @param response the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeInferenceExperimentResponse response,
            final ProxyClient<SageMakerClient> proxyClient) {

        final ResourceModel model = TranslatorForResponse.translateFromReadResponse(response);
        final ListTagsResponse tagsResponse = TagHelper.listResourceTags(
                TranslatorForRequest.translateToListTagsRequest(response.arn()), proxyClient);
        model.setTags(TranslatorForResponse.translate(tagsResponse.tags()));
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
