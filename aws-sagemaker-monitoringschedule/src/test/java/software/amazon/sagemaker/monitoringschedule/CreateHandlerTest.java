package software.amazon.sagemaker.monitoringschedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.MonitoringScheduleConfig;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ScheduleConfig;
import software.amazon.awssdk.services.sagemaker.model.ScheduleStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends software.amazon.sagemaker.monitoringschedule.AbstractTestBase {

    private final ResourceModel requestModel = ResourceModel.builder()
            .creationTime(TEST_TIME.toString())
            .endpointName(TEST_ENDPOINT_NAME)
            .monitoringScheduleName(TEST_SCHEDULE_NAME)
            .monitoringScheduleStatus(ScheduleStatus.SCHEDULED.toString())
            .lastModifiedTime(TEST_TIME.toString())
            .build();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SageMakerClient> proxyClient;

    @Mock
    SageMakerClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
        final DescribeMonitoringScheduleResponse describeMonitoringScheduleResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.SCHEDULED)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final CreateMonitoringScheduleResponse createMonitoringScheduleResponse = CreateMonitoringScheduleResponse.builder()
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(describeMonitoringScheduleResponse);
        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenReturn(createMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .endpointName(TEST_ENDPOINT_NAME)
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .monitoringScheduleStatus(ScheduleStatus.SCHEDULED.toString())
                .lastModifiedTime(TEST_TIME.toString())
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_WithJobDefinitionName_Success() {
        ResourceModel model = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .endpointName(TEST_ENDPOINT_NAME)
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .monitoringScheduleStatus(ScheduleStatus.SCHEDULED.toString())
                .monitoringScheduleConfig(software.amazon.sagemaker.monitoringschedule.MonitoringScheduleConfig.builder()
                        .monitoringJobDefinitionName(TEST_JOB_DEFINITION_NAME)
                        .monitoringType(TEST_MONITORING_TYPE)
                        .scheduleConfig(software.amazon.sagemaker.monitoringschedule.ScheduleConfig.builder()
                                .scheduleExpression("cron(0 12 ? * * *)").build())
                        .build())
                .lastModifiedTime(TEST_TIME.toString())
                .build();

        final DescribeMonitoringScheduleResponse describeMonitoringScheduleResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.SCHEDULED)
                        .lastModifiedTime(TEST_TIME)
                        .monitoringScheduleConfig(MonitoringScheduleConfig.builder()
                                .monitoringJobDefinitionName(TEST_JOB_DEFINITION_NAME)
                                .monitoringType(TEST_MONITORING_TYPE)
                                .scheduleConfig(ScheduleConfig.builder()
                                        .scheduleExpression("cron(0 12 ? * * *)").build())
                                .build())
                        .build();

        final CreateMonitoringScheduleResponse createMonitoringScheduleResponse = CreateMonitoringScheduleResponse.builder()
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(describeMonitoringScheduleResponse);
        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenReturn(createMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .endpointName(TEST_ENDPOINT_NAME)
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .monitoringScheduleStatus(ScheduleStatus.SCHEDULED.toString())
                .lastModifiedTime(TEST_TIME.toString())
                .monitoringScheduleConfig(software.amazon.sagemaker.monitoringschedule.MonitoringScheduleConfig.builder()
                        .monitoringJobDefinitionName(TEST_JOB_DEFINITION_NAME)
                        .monitoringType(TEST_MONITORING_TYPE)
                        .scheduleConfig(software.amazon.sagemaker.monitoringschedule.ScheduleConfig.builder()
                                .scheduleExpression("cron(0 12 ? * * *)").build())
                        .build())
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ScheduleAlreadyExists_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_SCHEDULE_NAME));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsServiceException validationFailureException = SageMakerException.builder()
                .message("1 validation error detected: Value null at 'monitoringScheduleName' " +
                            "failed to satisfy constraint: Member must not be null")
                .statusCode(400)
                .build();

        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_SuccessfulSchedule() {
        final DescribeMonitoringScheduleResponse firstDescribeResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.PENDING)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final DescribeMonitoringScheduleResponse secondDescribeResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.SCHEDULED)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final CreateMonitoringScheduleResponse createMonitoringScheduleResponse = CreateMonitoringScheduleResponse.builder()
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenReturn(createMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .endpointName(TEST_ENDPOINT_NAME)
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .monitoringScheduleStatus(ScheduleStatus.SCHEDULED.toString())
                .lastModifiedTime(TEST_TIME.toString())
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_VerifyStabilization_FailedSchedule() {
        final DescribeMonitoringScheduleResponse firstDescribeResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.PENDING)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final DescribeMonitoringScheduleResponse secondDescribeResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.FAILED)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final CreateMonitoringScheduleResponse createMonitoringScheduleResponse = CreateMonitoringScheduleResponse.builder()
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createMonitoringSchedule(any(CreateMonitoringScheduleRequest.class)))
                .thenReturn(createMonitoringScheduleResponse);
        
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing of {\"/properties/MonitoringScheduleArn\":\"testScheduleArn\"}"));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}