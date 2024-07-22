package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.TrackingServerStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
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
 * CloudFormation resource handler to be invoked when creating a new AWS::SageMaker::MlflowTrackingServer resource.
 */
public class CreateHandler extends BaseHandlerStd {

    private Logger logger;
    private final Delay delay;
    // For 60 minutes, check state every 2 minutes. Tracking Server Creates takes ~35 min on average, so a 2 min delay
    // will increase average CFN Create requests to ~37 min.
    protected static final Constant BACKOFF_STRATEGY = Constant.of().timeout(Duration.ofMinutes(60)).delay(Duration.ofMinutes(2)).build();

    public CreateHandler() {
        this.delay = BACKOFF_STRATEGY;
    }

    public CreateHandler(Delay delay) {
        // use a custom delay for unit test
        this.delay = delay;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger
    ) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-SageMaker-MlflowTrackingServer::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .backoffDelay(this.delay)
                    .makeServiceCall((createRequest, requestProxyClient) -> checkIfExistingAndCreate(model, createRequest, requestProxyClient))
                    .stabilize(this::stabilizedOnCreate)
                    .handleError((createRequest, exception, client, resourceModel, errorCallbackContext) -> {
                        // Special handling for Throttling Exception to force CloudFormation to retry.
                        // ThrottlingException is not a SageMaker Exception type, but rather a general AWS Exception type.
                        this.logger.log(String.format("Create Handler experienced error for Tracking Server %s caused by the exception -- %s",
                                resourceModel.getTrackingServerName(), exception.getMessage()));
                        final BaseHandlerException cfnException = ExceptionMapper.getCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME,
                                createRequest.trackingServerName(), (AwsServiceException) exception);
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
                    .progress())
            .then(progress -> TagHelper.updateTags(progress, proxy, request, callbackContext, proxyClient, logger, Action.CREATE))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * First checks if the target resource exists already. Afterwards, invokes the create request
     * using the provided proxyClient.
     *
     * @param model                             the CloudFormation resource model
     * @param createMlflowTrackingServerRequest the aws service request to create an mlflow tracking server
     * @param proxyClient                       the aws client used to make service calls
     * @return CreateMlflowTrackingServerResponse aws service response from creating the mlflow tracking server resource
     */
    private CreateMlflowTrackingServerResponse checkIfExistingAndCreate(
            final ResourceModel model,
            final CreateMlflowTrackingServerRequest createMlflowTrackingServerRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        try {
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                    proxyClient.client()::describeMlflowTrackingServer);
            logger.log(String.format("Found an existing Tracking Server by the same name: %s", model.getTrackingServerArn()));
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, createMlflowTrackingServerRequest.trackingServerName());
        } catch (final ResourceNotFoundException e) {
            logger.log(String.format("No existing %s [%s] found. Proceeding to create.", ResourceModel.TYPE_NAME,
                    createMlflowTrackingServerRequest.trackingServerName()));
        }
        return proxyClient.injectCredentialsAndInvokeV2(createMlflowTrackingServerRequest, proxyClient.client()::createMlflowTrackingServer);
    }

    /**
     * Stabilization method to ensure that a newly created tracking server resource has moved from CREATE_PENDING status to CREATED.
     * @param createMlflowTrackingServerRequest the aws service request to create an mlflow tracking server
     * @param createMlflowTrackingServerResponse the aws service response from creating an mlflow tracking server resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the mlflow tracking server resource has stabilized or not
     */
    private boolean stabilizedOnCreate(
            final CreateMlflowTrackingServerRequest createMlflowTrackingServerRequest,
            final CreateMlflowTrackingServerResponse createMlflowTrackingServerResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if (model.getTrackingServerArn() == null) {
            model.setTrackingServerArn(createMlflowTrackingServerResponse.trackingServerArn());
        }

        final TrackingServerStatus mlflowTrackingServerStatus = proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToReadRequest(model), proxyClient.client()::describeMlflowTrackingServer).trackingServerStatus();

        switch (mlflowTrackingServerStatus) {
            case CREATE_FAILED:
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getTrackingServerArn());
            case CREATED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getTrackingServerArn(), mlflowTrackingServerStatus));
                return true;
            case CREATING:
                logger.log(String.format("%s [%s] is stabilizing %s.", ResourceModel.TYPE_NAME,
                        model.getTrackingServerArn(), mlflowTrackingServerStatus));
                return false;
            default:
                throw new CfnGeneralServiceException(
                        String.format("Stabilizing of %s failed with an unexpected status %s",
                                model.getTrackingServerArn(), mlflowTrackingServerStatus));
        }
    }
}
