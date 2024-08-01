package software.amazon.sagemaker.mlflowtrackingserver;

import java.util.List;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


/**
 * CloudFormation resource handler to be invoked when reading a new AWS::SageMaker::MlflowTrackingServer resource.
 */
public class ReadHandler extends BaseHandlerStd {

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

        return proxy.initiate("AWS-SageMaker-MlflowTrackingServer::Read", proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall(this::readResource)
            .handleError((readRequest, exception, client, resourceModel, errorCallbackContext) -> {
                this.logger.log(String.format("Read Handler failed for Tracking Server %s caused by the exception -- %s",
                        resourceModel.getTrackingServerName(), exception.getMessage()));
                final BaseHandlerException cfnException = ExceptionMapper.getCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                        readRequest.trackingServerName(), (AwsServiceException) exception);
                // Return ProgressEvent with status FAILED and errorCode cfnException.getErrorCode().
                return ProgressEvent.defaultFailureHandler(cfnException, cfnException.getErrorCode());
            })
            .done((awsResponse) -> constructResourceModelFromResponse(awsResponse, proxyClient));
    }

    /**
     * Invokes the read request using the provided proxyClient.
     * @param describeMlflowTrackingServerRequest the aws service request to read an mlflow tracking server
     * @param proxyClient the aws client used to make service calls
     * @return describeMlflowTrackingServerResponse aws service response from reading an mlflow tracking server resource
     */
    private DescribeMlflowTrackingServerResponse readResource(
            final DescribeMlflowTrackingServerRequest describeMlflowTrackingServerRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        logger.log(String.format("Describe Tracking Server %s", describeMlflowTrackingServerRequest.trackingServerName()));
        return proxyClient.injectCredentialsAndInvokeV2(describeMlflowTrackingServerRequest,
                    proxyClient.client()::describeMlflowTrackingServer);
    }

    /**
     * Construct the CloudFormation resource model from the service read response by translating and adding tags.
     * @param describeMlflowTrackingServerResponse response from mlflow tracking server read request
     * @param proxyClient the aws client used to make service calls
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeMlflowTrackingServerResponse describeMlflowTrackingServerResponse,
            final ProxyClient<SageMakerClient> proxyClient) {
        final ResourceModel model = Translator.translateFromReadResponse(describeMlflowTrackingServerResponse);
        logger.log(String.format("Getting tags for Tracking Server %s", model.getTrackingServerArn()));
        addTagsToModel(model, proxyClient);
        return ProgressEvent.defaultSuccessHandler(model);
    }

    /**
     * Fetch and add tags to the mlflow tracking server resource model.
     * @param model CFN resource model for an mlflow tracking server
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
