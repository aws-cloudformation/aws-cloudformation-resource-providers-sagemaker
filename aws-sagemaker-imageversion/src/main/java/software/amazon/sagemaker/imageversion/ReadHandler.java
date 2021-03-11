package software.amazon.sagemaker.imageversion;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when reading a new AWS::SageMaker::ImageVersion resource.
 */
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

        return proxy.initiate("AWS-SageMaker-ImageVersion::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall(this::readImageVersion)
                .done((awsResponse) -> constructResourceModelFromResponse(awsResponse, proxyClient));
    }

    /**
     * Invokes the read request using the provided proxyClient.
     * @param describeImageVersionRequest the aws service request to read an image version
     * @param proxyClient the aws client used to make service calls
     * @return describeImageVersionResponse aws service response from reading an image version resource
     */
    private DescribeImageVersionResponse readImageVersion(
            final DescribeImageVersionRequest describeImageVersionRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final DescribeImageVersionResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(describeImageVersionRequest,
                    proxyClient.client()::describeImageVersion);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    getImageVersionIdentifier(describeImageVersionRequest.imageName(),
                            describeImageVersionRequest.version()), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    getImageVersionIdentifier(describeImageVersionRequest.imageName(),
                            describeImageVersionRequest.version()), e);
        }
        return response;
    }

    /**
     * Construct the CloudFormation resource model from the service read response by translating.
     * @param describeImageVersionResponse response from image version read request
     * @param proxyClient the aws client used to make service calls
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeImageVersionResponse describeImageVersionResponse,
            final ProxyClient<SageMakerClient> proxyClient) {
        final ResourceModel model = Translator.translateFromReadResponse(describeImageVersionResponse);
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
