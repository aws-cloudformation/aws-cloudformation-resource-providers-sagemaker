package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.TrackingServerStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.UpdateMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateMlflowTrackingServerResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
 * CloudFormation resource handler to be invoked when updating an existing AWS::SageMaker::MlflowTrackingServer resource.
 */
public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;
    private final Delay delay;
    // For 60 minutes, check state every 2 minutes. Tracking Server Updates can take varying lengths of time.
    protected static final Constant BACKOFF_STRATEGY = Constant.of().timeout(Duration.ofMinutes(60)).delay(Duration.ofMinutes(2)).build();

    public UpdateHandler() {
        this.delay = BACKOFF_STRATEGY;
    }

    public UpdateHandler(Delay delay) {
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
                        proxy.initiate("AWS-SageMaker-MlflowTrackingServer::Update", proxyClient, model, progress.getCallbackContext())
                                .translateToServiceRequest(resourceModel -> getExistingStateAndTranslate(resourceModel, proxyClient))
                                .backoffDelay(this.delay)
                                .makeServiceCall(this::updateMlflowTrackingServer)
                                .stabilize(this::stabilizedOnUpdate)
                                .handleError((updateRequest, exception, client, resourceModel, errorCallbackContext) -> {
                                    // Special handling for Throttling Exception to force CloudFormation to retry.
                                    // ThrottlingException is not a SageMaker Exception type, but rather a general AWS Exception type.
                                    this.logger.log(String.format("Update Handler experienced error for Tracking Server %s caused by the exception -- %s",
                                            resourceModel.getTrackingServerName(), exception.getMessage()));
                                    final BaseHandlerException cfnException = ExceptionMapper.getCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME,
                                            updateRequest.trackingServerName(), (AwsServiceException) exception);
                                    if (cfnException.getErrorCode().equals(HandlerErrorCode.Throttling)) {
                                        // Gets returned as a ProgressEvent with status IN_PROGRESS and errorCode Throttling.
                                        this.logger.log("Returning status IN_PROGRESS due to Throttling Error, to make CloudFormation retry.");
                                        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                                                .callbackContext(errorCallbackContext)
                                                .resourceModel(resourceModel)
                                                .errorCode(HandlerErrorCode.Throttling)
                                                .status(OperationStatus.IN_PROGRESS)
                                                .callbackDelaySeconds(10)
                                                .build();
                                    }
                                    return ProgressEvent.defaultFailureHandler(cfnException, cfnException.getErrorCode());
                                })
                                .progress()
                )
                .then(progress -> updateTagsForTrackingServer(progress, proxy, request, callbackContext, proxyClient, logger))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Read the current state of the mlflow tracking server resource and use in conjunction with desired state to format an update.
     * mlflow tracking server request.
     * @param requestedState CFN resource model representing the desired state of the resource
     * @param proxyClient the aws client used to make service calls
     * @return updateMlflowTrackingServerRequest the update request to be invoked by service
     */
    private UpdateMlflowTrackingServerRequest getExistingStateAndTranslate(
            final ResourceModel requestedState,
            final ProxyClient<SageMakerClient> proxyClient
    ) {
        final DescribeMlflowTrackingServerResponse existingState;
        try {
            existingState = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(requestedState),
                    proxyClient.client()::describeMlflowTrackingServer);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, requestedState.getTrackingServerName(), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    requestedState.getTrackingServerName(), e);
        }
        return Translator.translateToUpdateRequest(requestedState, Translator.translateFromReadResponse(existingState));
    }

    /**
     * Invokes the update request using the provided proxyClient.
     * @param updateMlflowTrackingServerRequest the aws service request to update an mlflow tracking server
     * @param proxyClient the aws client used to make service calls
     * @return updateMlflowTrackingServerResponse aws service response from updating an mlflow tracking server resource
     */
    private UpdateMlflowTrackingServerResponse updateMlflowTrackingServer(
            final UpdateMlflowTrackingServerRequest updateMlflowTrackingServerRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        return proxyClient.injectCredentialsAndInvokeV2(updateMlflowTrackingServerRequest, proxyClient.client()::updateMlflowTrackingServer);
    }

    /**
     * Stabilization method to ensure that a recently updated mlflow tracking server resource has moved from UPDATING status to CREATED.
     * @param updateMlflowTrackingServerRequest the aws service request to update an mlflow tracking server
     * @param updateMlflowTrackingServerResponse the aws service response from updating an mlflow tracking server resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the mlflow tracking server resource has stabilized or not
     */
    private boolean stabilizedOnUpdate(
            final UpdateMlflowTrackingServerRequest updateMlflowTrackingServerRequest,
            final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext
    ) {

        if (model.getTrackingServerArn() == null) {
            model.setTrackingServerArn(updateMlflowTrackingServerResponse.trackingServerArn());
        }

        final TrackingServerStatus trackingServerStatus = proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToReadRequest(model), proxyClient.client()::describeMlflowTrackingServer).trackingServerStatus();

        switch (trackingServerStatus) {
            case UPDATE_FAILED:
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getTrackingServerArn());
            case CREATED:
                // Add CREATED to "stabilized" states because the tracking server continues to exist in CREATED state
                // if the desired model is exactly the same as the previous model.
            case UPDATED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getTrackingServerArn(), trackingServerStatus));
                return true;
            case UPDATING:
                logger.log(String.format("%s [%s] is stabilizing %s.", ResourceModel.TYPE_NAME,
                        model.getTrackingServerArn(), trackingServerStatus));
                return false;
            default:
                throw new CfnGeneralServiceException(
                        String.format("Stabilizing of %s failed with an unexpected status %s",
                                model.getTrackingServerArn(), trackingServerStatus));
        }
    }

    /**
     * Handles updating tags on the mlflow tracking server resource if changes are present.
     * @param progress the ProgressEvent object to invoke CloudFormation Handler actions
     * @param proxy the aws client used to initiate the call chain
     * @param request the update request received through the handler
     * @param callbackContext the callback context
     * @param proxyClient the aws client used to make service calls
     * @param logger the logger object
     * @return progressEvent, in progress with delay callback and model state
     */
    private ProgressEvent<ResourceModel, CallbackContext> updateTagsForTrackingServer(
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger
    ) {
        if (TagHelper.shouldUpdateTags(request, logger)) {
            return TagHelper.updateTags(progress, proxy, request, callbackContext, proxyClient, logger, Action.UPDATE);
        } else {
            return progress;
        }
    }
}
