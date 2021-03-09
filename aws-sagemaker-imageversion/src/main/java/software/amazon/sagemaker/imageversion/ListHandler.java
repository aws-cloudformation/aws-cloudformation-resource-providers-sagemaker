package software.amazon.sagemaker.imageversion;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListImageVersionsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListImageVersionsResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when listing AWS::SageMaker::ImageVersion resources.
 */
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

        return proxy.initiate("AWS-SageMaker-ImageVersion::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> Translator.translateToListRequest(resourceModel, request.getNextToken()))
                .makeServiceCall(this::listImageVersions)
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Invokes the list image versions request using the provided proxyClient.
     * @param listImageVersionsRequest the aws service request to list image versions
     * @param proxyClient the aws client used to make service calls
     * @return listImageVersionsResponse aws service response from listing image version resources
     */
    private ListImageVersionsResponse listImageVersions(
            final ListImageVersionsRequest listImageVersionsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final ListImageVersionsResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(listImageVersionsRequest,
                    proxyClient.client()::listImageVersions);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.LIST.toString(), ResourceModel.TYPE_NAME,
                    listImageVersionsRequest.imageName(), e);
        }
        return response;
    }

    /**
     * Translates a list image versions response into the CFN resource model format and updates the model.
     * @param listImageVersionsResponse the aws service response from list image versions request
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListImageVersionsResponse listImageVersionsResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listImageVersionsResponse.nextToken())
                .resourceModels(Translator.translateFromListResponse(listImageVersionsResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
