package software.amazon.sagemaker.monitoringschedule;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.ScheduleStatus;
import software.amazon.awssdk.services.sagemaker.model.UpdateMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateMonitoringScheduleResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    private static final String OPERATION = "SageMaker::UpdateMonitoringSchedule";
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SageMaker-MonitoringSchedule::Update", proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToUpdateRequest)
                                .makeServiceCall(this::updateResource)
                                .stabilize(this::stabilizedOnUpdate)
                                .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the update request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param awsRequest the aws service request to update a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse update resource response
     */
    private UpdateMonitoringScheduleResponse updateResource(
            final UpdateMonitoringScheduleRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        try {
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::updateMonitoringSchedule);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.monitoringScheduleName(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(OPERATION, e);
        }
    }

    /**
     * This is used to ensure MonitoringSchedule resource has moved from Pending to any terminal state
     * (e.g. Scheduled, Stopped).
     * @param updateMonitoringScheduleRequest the aws service request to update a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnUpdate(
            final UpdateMonitoringScheduleRequest updateMonitoringScheduleRequest,
            final UpdateMonitoringScheduleResponse updateMonitoringScheduleResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if(model.getMonitoringScheduleArn() == null){
            model.setMonitoringScheduleArn(updateMonitoringScheduleResponse.monitoringScheduleArn());
        }

        final ScheduleStatus monitoringScheduleState = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeMonitoringSchedule).monitoringScheduleStatus();

        switch (monitoringScheduleState) {
            case SCHEDULED:
            case STOPPED:
                logger.log(String.format("%s [%s] has been stabilized with state %s during update operation.",
                        ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), monitoringScheduleState));
                return true;
            case PENDING:
                logger.log(String.format("%s [%s] is stabilizing during update.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing during update of " + model.getPrimaryIdentifier());

        }
    }
}