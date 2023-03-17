package software.amazon.sagemaker.inferenceexperiment;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListInferenceExperimentsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListInferenceExperimentsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-SageMaker-InferenceExperiment::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> TranslatorForRequest.translateToListRequest(request.getNextToken()))
                .makeServiceCall(this::listResources)
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the list request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param request the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return list resource response
     */
    private ListInferenceExperimentsResponse listResources(
            final ListInferenceExperimentsRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        ListInferenceExperimentsResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::listInferenceExperiments);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * Build the Progress Event object from the SageMaker ListInferenceExperiment response.
     * @param response the aws service list resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListInferenceExperimentsResponse response) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(response.nextToken())
                .resourceModels(TranslatorForResponse.translateFromListResponse(response))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}