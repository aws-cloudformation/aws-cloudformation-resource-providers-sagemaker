package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.TrackingServerStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Delay;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

/**
 * CloudFormation resource handler to be invoked when deleting an existing AWS::SageMaker::MlflowTrackingServer resource.
 */
public class DeleteHandler extends BaseHandlerStd {

    private Logger logger;
    private final Delay delay;
    // For 60 minutes, check state every 2 minutes. Tracking Server Deletes take ~20 min on average, so a 2 min delay
    // will increase average CFN Delete requests to ~22 mins.
    protected static final Constant BACKOFF_STRATEGY = Constant.of().timeout(Duration.ofMinutes(60)).delay(Duration.ofMinutes(2)).build();

    public DeleteHandler() {
        this.delay = BACKOFF_STRATEGY;
    }

    public DeleteHandler(Delay delay) {
        // use a custom delay for unit test
        this.delay = delay;
    }

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SageMakerClient> proxyClient,
            Logger logger
    ) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SageMaker-MlflowTrackingServer::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .backoffDelay(this.delay)
                                .makeServiceCall((deleteMlflowTrackingServerRequest, prxyClient) -> deleteMlflowTrackingServer(model, deleteMlflowTrackingServerRequest, prxyClient))
                                .stabilize(this::stabilizedOnDelete)
                                .handleError((deleteRequest, exception, client, resourceModel, errorCallbackContext) -> {
                                    // Special handling for Throttling Exception to force CloudFormation to retry.
                                    // ThrottlingException is not a SageMaker Exception type, but rather a general AWS Exception type.
                                    this.logger.log(String.format("Delete Handler experienced error for Tracking Server %s caused by the exception -- %s",
                                            resourceModel.getTrackingServerName(), exception.getMessage()));
                                    final BaseHandlerException cfnException = ExceptionMapper.getCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME,
                                            deleteRequest.trackingServerName(), (AwsServiceException) exception);
                                    if (cfnException.getErrorCode().equals(HandlerErrorCode.Throttling)) {
                                        // Return ProgressEvent with status IN_PROGRESS and errorCode Throttling.
                                        this.logger.log("Returning status IN_PROGRESS due to Throttling Error, to make CloudFormation retry.");
                                        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                                                .callbackContext(errorCallbackContext)
                                                .resourceModel(resourceModel)
                                                .errorCode(HandlerErrorCode.Throttling)
                                                .status(OperationStatus.IN_PROGRESS)
                                                .callbackDelaySeconds(10)
                                                .build();
                                    }
                                    // Return ProgressEvent with status FAILED and errorCode cfnException.getErrorCode().
                                    return ProgressEvent.defaultFailureHandler(cfnException, cfnException.getErrorCode());
                                })
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Checks that the mlflow tracking server is not already deleting and invokes the delete request using the provided proxyClient.
     * @param model the CloudFormation resource model
     * @param deleteMlflowTrackingServerRequest the aws service request to delete an mlflow tracking server
     * @param proxyClient the aws client used to make service calls
     * @return deleteMlflowTrackingServerResponse aws service response from deleting an mlflow tracking server resource
     */
    private DeleteMlflowTrackingServerResponse deleteMlflowTrackingServer(
            final ResourceModel model,
            final DeleteMlflowTrackingServerRequest deleteMlflowTrackingServerRequest,
            final ProxyClient<SageMakerClient> proxyClient
    ) {
        DeleteMlflowTrackingServerResponse response = null;
        final TrackingServerStatus currentStatus = proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToReadRequest(model), proxyClient.client()::describeMlflowTrackingServer).trackingServerStatus();
        if (currentStatus.equals(TrackingServerStatus.DELETING)) {
            response = DeleteMlflowTrackingServerResponse.builder().build();
            return response;
        }
        response = proxyClient.injectCredentialsAndInvokeV2(deleteMlflowTrackingServerRequest, proxyClient.client()::deleteMlflowTrackingServer);
        return response;
    }

    /**
     * Stabilization method to ensure that a recently delete mlflow tracking server resource no longer exists.
     * @param deleteMlflowTrackingServerRequest the aws service request to delete an mlflow tracking server
     * @param deleteMlflowTrackingServerResponse the aws service response from deleting an mlflow tracking server resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the mlflow tracking server resource has stabilized after deletion (no longer found)
     */
    private boolean stabilizedOnDelete(
            final DeleteMlflowTrackingServerRequest deleteMlflowTrackingServerRequest,
            final DeleteMlflowTrackingServerResponse deleteMlflowTrackingServerResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        final TrackingServerStatus trackingServerStatus;
        try {
            trackingServerStatus = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToReadRequest(model), proxyClient.client()::describeMlflowTrackingServer).trackingServerStatus();
        } catch (final ResourceNotFoundException e) {
            logger.log(String.format("Tracking Server %s has successfully been deleted. Stabilizer is returning true.",
                    model.getTrackingServerName()));
            return true;
        }

        switch (trackingServerStatus) {
            case DELETING:
                logger.log(String.format("%s with name [%s] is stabilizing while delete.",
                        ResourceModel.TYPE_NAME, model.getTrackingServerName()));
                return false;
            case DELETE_FAILED:
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getTrackingServerName());
            default:
                throw new CfnGeneralServiceException(String.format(
                        "Unexpected status: [%s] while stabilizing delete for resource: [%s]",
                        trackingServerStatus, model.getTrackingServerArn()));
        }
    }
}
