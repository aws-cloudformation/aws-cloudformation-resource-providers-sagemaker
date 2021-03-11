package software.amazon.sagemaker.image;

import java.util.List;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


/**
 * CloudFormation resource handler to be invoked when reading a new AWS::SageMaker::Image resource.
 */
public class ReadHandler extends BaseHandlerStd {

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

        return proxy.initiate("AWS-SageMaker-Image::Read", proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall(this::readImage)
            .done((awsResponse) -> constructResourceModelFromResponse(awsResponse, proxyClient));
    }

    /**
     * Invokes the read request using the provided proxyClient.
     * @param describeImageRequest the aws service request to read an image
     * @param proxyClient the aws client used to make service calls
     * @return describeImageResponse aws service response from reading an image resource
     */
    private DescribeImageResponse readImage(
            final DescribeImageRequest describeImageRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final DescribeImageResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(describeImageRequest,
                    proxyClient.client()::describeImage);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, describeImageRequest.imageName(), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    describeImageRequest.imageName(), e);
        }
        return response;
    }

    /**
     * Construct the CloudFormation resource model from the service read response by translating and adding tags.
     * @param describeImageResponse response from image read request
     * @param proxyClient the aws client used to make service calls
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeImageResponse describeImageResponse,
            final ProxyClient<SageMakerClient> proxyClient) {
        final ResourceModel model = Translator.translateFromReadResponse(describeImageResponse);
        addTagsToModel(model, proxyClient);
        return ProgressEvent.defaultSuccessHandler(model);
    }

    /**
     * Fetch and add tags to the image resource model.
     * @param model CFN resource model for an image
     * @param proxyClient the aws client used to make service calls
     */
    private void addTagsToModel(final ResourceModel model, final ProxyClient<SageMakerClient> proxyClient) {
        final ListTagsResponse tagsResponse = proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToListTagsRequest(model), proxyClient.client()::listTags);
        final List<Tag> tags = Translator.sdkTagsToCfnTags(tagsResponse.tags());
        if (!tags.isEmpty()) {
            model.setTags(tags);
        }
    }
}
