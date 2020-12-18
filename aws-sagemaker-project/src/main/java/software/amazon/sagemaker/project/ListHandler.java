package software.amazon.sagemaker.project;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListProjectsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListProjectsResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class ListHandler extends BaseHandlerStd {

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

        return proxy.initiate("AWS-SageMaker-Project::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> Translator.translateToListRequest(request.getNextToken()))
                .makeServiceCall((awsRequest, sdkProxyClient) -> listResources(awsRequest, sdkProxyClient))
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the list request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param listProjectsRequest the aws service request to list projects
     * @param proxyClient the aws service client to make the call
     * @return list project response
     */
    private ListProjectsResponse listResources(
            final ListProjectsRequest listProjectsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        ListProjectsResponse listProjectsResponse = null;
        try {
            listProjectsResponse =  proxyClient.injectCredentialsAndInvokeV2(listProjectsRequest,
                    proxyClient.client()::listProjects);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.LIST.toString(), e);
        }

        return listProjectsResponse;
    }

    /**
     * Build the Progress Event object from the SageMaker ListProjectsResult.
     * @param listResponse the aws service list resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListProjectsResponse listResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listResponse.nextToken())
                .resourceModels(Translator.translateFromListResponse(listResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }

}
