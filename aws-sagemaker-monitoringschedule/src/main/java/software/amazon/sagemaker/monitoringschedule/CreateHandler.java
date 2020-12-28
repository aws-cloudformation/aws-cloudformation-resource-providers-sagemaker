package software.amazon.sagemaker.monitoringschedule;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ScheduleStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

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
                    proxy.initiate("AWS-SageMaker-MonitoringSchedule::Create", proxyClient, model, callbackContext)
                        .translateToServiceRequest(TranslatorForRequest::translateToCreateRequest)
                        .makeServiceCall(this::createResource)
                        .stabilize(this::stabilizedOnCreate)
                        .progress())
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param awsRequest the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create resource response
     */
    private CreateMonitoringScheduleResponse createResource(
            final CreateMonitoringScheduleRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        CreateMonitoringScheduleResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createMonitoringSchedule);
        } catch (final ResourceInUseException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, awsRequest.monitoringScheduleName());
        } catch (final AwsServiceException e) {

            // The exception thrown due to validation failure does not have error code set,
            // hence we need to check it using error message
            if(StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("validation error detected")) {
                throw new CfnInvalidRequestException(Action.CREATE.toString(), e);
            }
            Translator.throwCfnException(Action.CREATE.toString(), e);
        }

        return response;
    }

    /**
     * This is used to ensure MonitoringSchedule resource has moved from Pending to Scheduled/Failed state.
     * @param awsRequest the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create resource response
     */
    private boolean stabilizedOnCreate(
        final CreateMonitoringScheduleRequest createMonitoringScheduleRequest,
        final CreateMonitoringScheduleResponse createMonitoringScheduleResponse,
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext) {

        if(model.getMonitoringScheduleArn() == null){
            model.setMonitoringScheduleArn(createMonitoringScheduleResponse.monitoringScheduleArn());
        }

        final ScheduleStatus monitoringScheduleState = proxyClient.injectCredentialsAndInvokeV2(TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeMonitoringSchedule).monitoringScheduleStatus();

        switch (monitoringScheduleState) {
            case SCHEDULED:
            case STOPPED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), monitoringScheduleState));
                return true;
            case PENDING:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing of " + model.getPrimaryIdentifier());

        }
    }

}