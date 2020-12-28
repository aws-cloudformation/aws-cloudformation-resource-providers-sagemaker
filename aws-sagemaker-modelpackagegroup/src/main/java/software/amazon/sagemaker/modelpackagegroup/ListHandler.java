package software.amazon.sagemaker.modelpackagegroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackageGroupsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackageGroupsResponse;
import software.amazon.cloudformation.Action;
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

        return proxy.initiate("AWS-SageMaker-ModelPackageGroup::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> Translator.translateToListRequest(request.getNextToken()))
                .makeServiceCall((awsRequest, sdkProxyClient) -> listResources(awsRequest, sdkProxyClient))
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the list request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param listModelPackageGroupsRequest the aws service request to list model package groups
     * @param proxyClient the aws service client to make the call
     * @return list model package group response
     */
    private ListModelPackageGroupsResponse listResources(
            final ListModelPackageGroupsRequest listModelPackageGroupsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        ListModelPackageGroupsResponse listModelPackageGroupsResponse = null;
        try {
            listModelPackageGroupsResponse =  proxyClient.injectCredentialsAndInvokeV2(listModelPackageGroupsRequest,
                    proxyClient.client()::listModelPackageGroups);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.LIST.toString(), e);
        }

        return listModelPackageGroupsResponse;
    }

    /**
     * Build the Progress Event object from the SageMaker ListModelPackageGroupsResult.
     * @param listResponse the aws service list resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListModelPackageGroupsResponse listResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listResponse.nextToken())
                .resourceModels(Translator.translateFromListResponse(listResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
