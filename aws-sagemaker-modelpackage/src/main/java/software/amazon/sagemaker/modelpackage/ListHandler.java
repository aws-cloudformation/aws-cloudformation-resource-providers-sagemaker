package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackagesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackagesResponse;
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
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<SageMakerClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-SageMaker-ModelPackage::List", proxyClient, model, callbackContext)
            .translateToServiceRequest(resourceModel -> Translator.translateToListRequest(request.getNextToken(), model))
            .makeServiceCall((awsRequest, sdkProxyClient) -> listResources(awsRequest, sdkProxyClient))
            .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the list request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param listModelPackagesRequest the aws service request to list model packages
     * @param proxyClient the aws service client to make the call
     * @return list model packages response
     */
    private ListModelPackagesResponse listResources(
        final ListModelPackagesRequest listModelPackagesRequest,
        final ProxyClient<SageMakerClient> proxyClient) {

        ListModelPackagesResponse listModelPackagesResponse = null;
        try {
            listModelPackagesResponse =  proxyClient.injectCredentialsAndInvokeV2(listModelPackagesRequest,
                proxyClient.client()::listModelPackages);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.LIST.toString(), e);
        }

        return listModelPackagesResponse;
    }

    /**
     * Build the Progress Event object from the SageMaker ListModelPackagesResult.
     * @param listResponse the aws service list resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
        final ListModelPackagesResponse listResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .nextToken(listResponse.nextToken())
            .resourceModels(Translator.translateFromListResponse(listResponse))
            .status(OperationStatus.SUCCESS)
            .build();
    }

}
