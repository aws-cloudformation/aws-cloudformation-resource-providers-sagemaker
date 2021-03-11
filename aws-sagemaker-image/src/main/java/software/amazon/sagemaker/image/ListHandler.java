package software.amazon.sagemaker.image;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListImagesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListImagesResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when listing AWS::SageMaker::Image resources.
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

        return proxy.initiate("AWS-SageMaker-Image::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> Translator.translateToListRequest(request.getNextToken()))
                .makeServiceCall(this::listImages)
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Invokes the list images request using the provided proxyClient.
     * @param listImagesRequest the aws service request to list images
     * @param proxyClient the aws client used to make service calls
     * @return listImagesResponse aws service response from listing image resources
     */
    private ListImagesResponse listImages(
            final ListImagesRequest listImagesRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final ListImagesResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(listImagesRequest, proxyClient.client()::listImages);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.LIST.toString(), ResourceModel.TYPE_NAME, "ListImages", e);
        }
        return response;
    }

    /**
     * Translates a list images response into the CFN resource model format and updates the model.
     * @param listImagesResponse the aws service response from list images request
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListImagesResponse listImagesResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listImagesResponse.nextToken())
                .resourceModels(Translator.translateFromListResponse(listImagesResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
