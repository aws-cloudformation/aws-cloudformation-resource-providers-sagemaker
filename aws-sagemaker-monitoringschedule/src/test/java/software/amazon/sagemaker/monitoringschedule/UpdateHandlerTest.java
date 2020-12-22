package software.amazon.sagemaker.monitoringschedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ScheduleStatus;
import software.amazon.awssdk.services.sagemaker.model.UpdateMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateMonitoringScheduleResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
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
public class UpdateHandlerTest extends software.amazon.sagemaker.monitoringschedule.AbstractTestBase {

    private static final String OPERATION = "SageMaker::UpdateMonitoringSchedule";

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
    public void testUpdateHandler_SimpleSuccess() {
        final DescribeMonitoringScheduleResponse describeMonitoringScheduleResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.SCHEDULED)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final UpdateMonitoringScheduleResponse updateMonitoringScheduleResponse = UpdateMonitoringScheduleResponse.builder()
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(describeMonitoringScheduleResponse);
        when(proxyClient.client().updateMonitoringSchedule(any(UpdateMonitoringScheduleRequest.class)))
                .thenReturn(updateMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
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
    public void testUpdateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().updateMonitoringSchedule(any(UpdateMonitoringScheduleRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                OPERATION));
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException() {
        when(proxyClient.client().updateMonitoringSchedule(any(UpdateMonitoringScheduleRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_SCHEDULE_NAME));
    }

    @Test
    public void testUpdateHandler_ResourceLimitExceededException() {
        when(proxyClient.client().updateMonitoringSchedule(any(UpdateMonitoringScheduleRequest.class)))
                .thenThrow(ResourceLimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                OPERATION));
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_SuccessfulUpdate() {
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
                        .monitoringScheduleStatus(ScheduleStatus.STOPPED)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final UpdateMonitoringScheduleResponse updateMonitoringScheduleResponse = UpdateMonitoringScheduleResponse.builder()
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateMonitoringSchedule(any(UpdateMonitoringScheduleRequest.class)))
                .thenReturn(updateMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .endpointName(TEST_ENDPOINT_NAME)
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .monitoringScheduleStatus(ScheduleStatus.STOPPED.toString())
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
    public void  testUpdateHandler_VerifyStabilization_FailedSchedule() {
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

        final UpdateMonitoringScheduleResponse updateMonitoringScheduleResponse = UpdateMonitoringScheduleResponse.builder()
                .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateMonitoringSchedule(any(UpdateMonitoringScheduleRequest.class)))
                .thenReturn(updateMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during update of {\"/properties/MonitoringScheduleArn\":\"testScheduleArn\"}"));
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .endpointName(TEST_ENDPOINT_NAME)
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .monitoringScheduleStatus(ScheduleStatus.SCHEDULED.toString())
                .lastModifiedTime(TEST_TIME.toString())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}