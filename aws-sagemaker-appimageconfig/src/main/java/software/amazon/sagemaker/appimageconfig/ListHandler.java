package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListAppImageConfigsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListAppImageConfigsResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-AppImageConfig::List";

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
                .translateToServiceRequest(resourceModel -> TranslatorForRequest.translateToListRequest(request.getNextToken()))
                .makeServiceCall(this::listResources)
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the list request through the proxyClient.
     *
     * @param awsRequest aws service list resource request
     * @param proxyClient aws service client used for making calls
     * @return list resource response
     */
    private ListAppImageConfigsResponse listResources(
            final ListAppImageConfigsRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        ListAppImageConfigsResponse response = null;
        try {
            response =  proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::listAppImageConfigs);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.LIST.toString(), ResourceModel.TYPE_NAME, null, e);
        }

        return response;
    }

    /**
     * Build the Progress Event object from the list response.
     *
     * @param listResponse the aws service list resource response
     * @return progressEvent indicating success
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListAppImageConfigsResponse listResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listResponse.nextToken())
                .resourceModels(TranslatorForResponse.translateFromListResponse(listResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
