package software.amazon.sagemaker.inferenceexperiment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentResponse;
import software.amazon.awssdk.utils.DateUtils;
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
public class DeleteHandlerTest extends AbstractTestBase {

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
        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Completed", null);

        final DeleteInferenceExperimentResponse deleteInferenceExperimentResponse = DeleteInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().deleteInferenceExperiment(any(DeleteInferenceExperimentRequest.class)))
                .thenReturn(deleteInferenceExperimentResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo((OperationStatus.SUCCESS));
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void testDeleteHandler_StopAndDeleteResource_Successful() {
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse describeResponseWithStoppingStatus =
                getSdkDescribeResponse("Stopping", null);
        final DescribeInferenceExperimentResponse describeResponseWithCompletedStatus =
                getSdkDescribeResponse("Completed", null);

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        final DeleteInferenceExperimentResponse deleteInferenceExperimentResponse = DeleteInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithStoppingStatus)
                .thenReturn(describeResponseWithCompletedStatus);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);
        when(proxyClient.client().deleteInferenceExperiment(any(DeleteInferenceExperimentRequest.class)))
                .thenReturn(deleteInferenceExperimentResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_StopResource_VerifyStabilization_Successful() {
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse describeResponseWithStoppingStatus =
                getSdkDescribeResponse("Stopping", null);
        final DescribeInferenceExperimentResponse describeResponseWithCompletedStatus =
                getSdkDescribeResponse("Completed", null);

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        final DeleteInferenceExperimentResponse deleteInferenceExperimentResponse = DeleteInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithStoppingStatus)
                .thenReturn(describeResponseWithCompletedStatus);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);
        when(proxyClient.client().deleteInferenceExperiment(any(DeleteInferenceExperimentRequest.class)))
                .thenReturn(deleteInferenceExperimentResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void  testDeleteHandler_StopResource_VerifyStabilization_Failed() {
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse describeResponseWithStoppingStatus =
                getSdkDescribeResponse("Stopping", null);
        final DescribeInferenceExperimentResponse describeResponseWithFailedStatus =
                getSdkDescribeResponse("Failed", null);

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithStoppingStatus)
                .thenReturn(describeResponseWithFailedStatus);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during stop of {\"/properties/Name\":\"testInferenceExperimentName\"}"));
    }

    @Test
    public void testDeleteHandler_DeleteResource_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().deleteInferenceExperiment(any(DeleteInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testDeleteHandler_DeleteResource_ResourceNotFoundException() {
        when(proxyClient.client().deleteInferenceExperiment(any(DeleteInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    @Test
    public void testDeleteHandler_StopResource_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Running", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Cancelled");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testDeleteHandler_StopResource_ResourceNotFoundException() {
        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Running", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Cancelled");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    @Test
    public void testDeleteHandler_ReadResource_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testDeleteHandler_ReadResource_ResourceNotFoundException() {
        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .endpointName(TEST_ENDPOINT_NAME)
                .endpointMetadata(getCfnEndpointMetadata())
                .schedule(getCfnSchedule())
                .dataStorageConfig(getCfnDataStorageConfig())
                .modelVariants(getCfnModelVariants())
                .shadowModeConfig(getCfnShadowModeConfig())
                .build();
    }

    private ResourceModel getResponseResourceModel(final String status, final String statusReason) {
        return ResourceModel.builder()
                .arn(TEST_EXPERIMENT_ARN)
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .endpointName(TEST_ENDPOINT_NAME)
                .endpointMetadata(getCfnEndpointMetadata())
                .schedule(getCfnSchedule())
                .dataStorageConfig(getCfnDataStorageConfig())
                .modelVariants(getCfnModelVariants())
                .shadowModeConfig(getCfnShadowModeConfig())
                .endpointMetadata(getCfnEndpointMetadata())
                .creationTime(DateUtils.formatIso8601Date(TEST_TIME))
                .lastModifiedTime(DateUtils.formatIso8601Date(TEST_TIME))
                .status(status)
                .statusReason(statusReason)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
