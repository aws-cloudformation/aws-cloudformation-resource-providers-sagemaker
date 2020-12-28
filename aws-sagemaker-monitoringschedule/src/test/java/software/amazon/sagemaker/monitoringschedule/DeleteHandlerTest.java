package software.amazon.sagemaker.monitoringschedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends software.amazon.sagemaker.monitoringschedule.AbstractTestBase {

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
    public void testDeleteHandler_SimpleSuccess() {
        final DeleteMonitoringScheduleResponse deleteMonitoringScheduleResponse = DeleteMonitoringScheduleResponse.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteMonitoringSchedule(any(DeleteMonitoringScheduleRequest.class)))
                .thenReturn(deleteMonitoringScheduleResponse);


        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo((OperationStatus.SUCCESS));
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void testDeleteHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().deleteMonitoringSchedule(any(DeleteMonitoringScheduleRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.DELETE));
    }

    @Test
    public void testDeleteHandler_ScheduleDoesNotExists_Fails() {
        when(proxyClient.client().deleteMonitoringSchedule(any(DeleteMonitoringScheduleRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_SCHEDULE_NAME));
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete() {
        final DescribeMonitoringScheduleResponse firstDescribeResponse =
                DescribeMonitoringScheduleResponse.builder()
                        .creationTime(TEST_TIME)
                        .endpointName(TEST_ENDPOINT_NAME)
                        .monitoringScheduleArn(TEST_SCHEDULE_ARN)
                        .monitoringScheduleName(TEST_SCHEDULE_NAME)
                        .monitoringScheduleStatus(ScheduleStatus.PENDING)
                        .lastModifiedTime(TEST_TIME)
                        .build();

        final DeleteMonitoringScheduleResponse deleteMonitoringScheduleResponse = DeleteMonitoringScheduleResponse.builder()
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(firstDescribeResponse).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteMonitoringSchedule(any(DeleteMonitoringScheduleRequest.class)))
                .thenReturn(deleteMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_ResourceNotDeleted() {
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

        final DeleteMonitoringScheduleResponse deleteMonitoringScheduleResponse = DeleteMonitoringScheduleResponse.builder()
                .build();

        when(proxyClient.client().describeMonitoringSchedule(any(DescribeMonitoringScheduleRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteMonitoringSchedule(any(DeleteMonitoringScheduleRequest.class)))
                .thenReturn(deleteMonitoringScheduleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Delete stabilizing of monitoring schedule: " + TEST_SCHEDULE_NAME));
    }


    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .monitoringScheduleName(TEST_SCHEDULE_NAME)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
