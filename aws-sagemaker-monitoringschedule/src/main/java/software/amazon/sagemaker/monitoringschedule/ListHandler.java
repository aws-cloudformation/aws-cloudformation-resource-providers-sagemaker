package software.amazon.sagemaker.monitoringschedule;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListMonitoringSchedulesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListMonitoringSchedulesResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    private static final String OPERATION = "SageMaker::ListMonitoringSchedule";
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-SageMaker-MonitoringSchedule::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> TranslatorForRequest.translateToListRequest(request.getNextToken()))
                .makeServiceCall((awsRequest, sdkProxyClient) -> listResources(awsRequest, sdkProxyClient))
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the list request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param listMonitoringScheduleRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return listMonitoringScheduleResponse
     */
    private ListMonitoringSchedulesResponse listResources(
            final ListMonitoringSchedulesRequest listMonitoringScheduleRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        ListMonitoringSchedulesResponse listMonitoringSchedulesResponse = null;
        try {
            listMonitoringSchedulesResponse =  proxyClient.injectCredentialsAndInvokeV2(listMonitoringScheduleRequest,
                    proxyClient.client()::listMonitoringSchedules);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(OPERATION, e);
        }

        return listMonitoringSchedulesResponse;
    }

    /**
     * Build the Progress Event object from the SageMaker ListMonitoringSchedules response.
     * @param listResponse the aws service list resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListMonitoringSchedulesResponse listResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listResponse.nextToken())
                .resourceModels(TranslatorForResponse.translateFromListResponse(listResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}