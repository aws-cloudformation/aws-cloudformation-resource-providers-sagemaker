package software.amazon.sagemaker.monitoringschedule;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.ScheduleStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

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
                        proxy.initiate("AWS-SageMaker-MonitoringSchedule::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .stabilize(this::stabilizedOnDelete)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     * @param deleteMonitoringScheduleRequest the aws service request to delete a resource
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private DeleteMonitoringScheduleResponse deleteResource(
            final DeleteMonitoringScheduleRequest deleteMonitoringScheduleRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteMonitoringScheduleResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(deleteMonitoringScheduleRequest, proxyClient.client()::deleteMonitoringSchedule);
        } catch (ResourceNotFoundException e) {
            // NotFound responded from Delete handler will be considered as success by CFN backend service.
            // This is to handle out of stack resource deletion (https://sage.amazon.com/questions/896677)
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteMonitoringScheduleRequest.monitoringScheduleName());
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.DELETE.toString(), e);
        }

        return response;
    }

    /**
     * Sync delete API moves resource in PENDING state and actual deletion happens asynchronously.
     * Stabilization is required to ensure MonitoringSchedule resource deletion has been completed.
     * @param deleteMonitoringScheduleRequest the aws service request to delete a resource
     * @param deleteMonitoringScheduleResponse the aws service response to delete a resource
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnDelete(
            final DeleteMonitoringScheduleRequest deleteMonitoringScheduleRequest,
            final DeleteMonitoringScheduleResponse deleteMonitoringScheduleResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            final ScheduleStatus monitoringScheduleState = proxyClient.injectCredentialsAndInvokeV2(TranslatorForRequest.translateToReadRequest(model),
                    proxyClient.client()::describeMonitoringSchedule).monitoringScheduleStatus();

            switch (monitoringScheduleState) {
                case PENDING:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.", ResourceModel.TYPE_NAME, model.getMonitoringScheduleName()));
                    return false;
                default:
                    throw new CfnGeneralServiceException("Delete stabilizing of monitoring schedule: " + model.getMonitoringScheduleName());
            }
        } catch (ResourceNotFoundException e) {
            return true;
        }
    }
}