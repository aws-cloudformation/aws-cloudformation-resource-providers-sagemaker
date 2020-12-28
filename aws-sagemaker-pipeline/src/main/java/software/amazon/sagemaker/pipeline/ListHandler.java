package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListPipelinesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListPipelinesResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Pipeline::List";
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger
    ) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> TranslatorForRequest.translateToListRequest(request.getNextToken()))
                .makeServiceCall(this::listResources)
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the list request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return listPipelinesResponse
     */
    private ListPipelinesResponse listResources(
            final ListPipelinesRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient
    ) {

        ListPipelinesResponse listPipelinesResponse = null;
        try {
            listPipelinesResponse =  proxyClient.injectCredentialsAndInvokeV2(awsRequest,
                    proxyClient.client()::listPipelines);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.LIST.toString(), ResourceModel.TYPE_NAME, null, e);
        }

        return listPipelinesResponse;
    }

    /**
     * Build the Progress Event object from the SageMaker ListPipelines response.
     * @param listResponse the aws service list resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListPipelinesResponse listResponse
    ) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listResponse.nextToken())
                .resourceModels(TranslatorForResponse.translateFromListResponse(listResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }

}
