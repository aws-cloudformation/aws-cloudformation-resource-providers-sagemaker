package software.amazon.sagemaker.monitoringschedule;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.ExecutionStatus;
import software.amazon.awssdk.services.sagemaker.model.MonitoringExecutionSummary;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ScheduleStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends software.amazon.sagemaker.monitoringschedule.AbstractTestBase {

    private static final String TEST_PROCESSING_JOB_ARN = "testProcessingJobArn";
    private static final String TEST_FAILURE_REASON = "Some failure reason";

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
    public void testReadHandler_SimpleSuccess() {
        MonitoringExecutionSummary responseExecutionSummary = MonitoringExecutionSummary.builder()
                .creationTime(TEST_TIME)
                .endpointName(TEST_ENDPOINT_NAME)
                .failureReason(TEST_FAILURE_REASON)
                .lastModifiedTime(TEST_TIME)
                .monitoringExecutionStatus(ExecutionStatus.COMPLETED)
                .processingJobArn(TEST_PROCESSING_JOB_ARN)
                .scheduledTime(TEST_TIME)
                .build();

        final DescribeMonitoringScheduleResponse describeMonitoringScheduleResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.SCHEDULED)
                        .lastMonitoringExecutionSummary(responseExecutionSummary)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(describeMonitoringScheduleResponse);

        software.amazon.sagemaker.monitoringschedule.MonitoringExecutionSummary resourceModelExecutionSummary =
                software.amazon.sagemaker.monitoringschedule.MonitoringExecutionSummary.builder()
                        .creationTime(TEST_TIME.toString())
                        .endpointName(TEST_ENDPOINT_NAME)
                        .failureReason(TEST_FAILURE_REASON)
                        .lastModifiedTime(TEST_TIME.toString())
                        .monitoringExecutionStatus(ExecutionStatus.COMPLETED.toString())
                        .processingJobArn(TEST_PROCESSING_JOB_ARN)
                        .scheduledTime(TEST_TIME.toString())
                        .build();

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .endpointName(TEST_ENDPOINT_NAME)
                .monitoringScheduleArn(TEST_ARN)
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .monitoringScheduleStatus(ScheduleStatus.SCHEDULED.toString())
                .lastMonitoringExecutionSummary(resourceModelExecutionSummary)
                .lastModifiedTime(TEST_TIME.toString())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedResourceModel);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_ScheduleDoesNotExist_Fails() {
        final AwsServiceException resourceNotFoundException = AwsServiceException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenThrow(resourceNotFoundException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_SCHEDULE_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .build();
    }
}