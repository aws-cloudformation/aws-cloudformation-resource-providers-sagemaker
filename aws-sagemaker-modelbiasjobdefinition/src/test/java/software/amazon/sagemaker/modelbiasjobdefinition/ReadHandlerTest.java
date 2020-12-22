package software.amazon.sagemaker.modelbiasjobdefinition;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelBiasJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelBiasBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends software.amazon.sagemaker.modelbiasjobdefinition.AbstractTestBase {

    private static final String TEST_PROCESSING_JOB_NAME = "testProcessingJobName";

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

        ModelBiasBaselineConfig modelBiasBaselineConfig = ModelBiasBaselineConfig.builder()
                .baseliningJobName(TEST_PROCESSING_JOB_NAME).build();

        final DescribeModelBiasJobDefinitionResponse describeModelBiasJobDefinitionResponse =
                DescribeModelBiasJobDefinitionResponse.builder()
                        .creationTime(TEST_TIME)
                        .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                        .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                        .modelBiasBaselineConfig(modelBiasBaselineConfig)
                        .roleArn(TEST_ARN)
                        .build();

        when(proxyClient.client().describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class)))
                .thenReturn(describeModelBiasJobDefinitionResponse);

        software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasBaselineConfig resourceBaselineConfig =
                software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasBaselineConfig.builder()
                .baseliningJobName(TEST_PROCESSING_JOB_NAME).build();

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                .modelBiasBaselineConfig(resourceBaselineConfig)
                .roleArn(TEST_ARN)
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
        verify(proxyClient.client()).describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class));
    }

    @Test
    public void testReadHandler_WithoutJobDefinitionName_Success() {

        ModelBiasBaselineConfig modelBiasBaselineConfig = ModelBiasBaselineConfig.builder()
                .baseliningJobName(TEST_PROCESSING_JOB_NAME).build();

        final DescribeModelBiasJobDefinitionResponse describeModelBiasJobDefinitionResponse =
                DescribeModelBiasJobDefinitionResponse.builder()
                        .creationTime(TEST_TIME)
                        .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                        .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                        .modelBiasBaselineConfig(modelBiasBaselineConfig)
                        .roleArn(TEST_ARN)
                        .build();
        ArgumentCaptor<DescribeModelBiasJobDefinitionRequest> requestCaptor = ArgumentCaptor.forClass(
                DescribeModelBiasJobDefinitionRequest.class);

        when(proxyClient.client().describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class)))
                .thenReturn(describeModelBiasJobDefinitionResponse);

        software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasBaselineConfig resourceBaselineConfig =
                software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasBaselineConfig.builder()
                        .baseliningJobName(TEST_PROCESSING_JOB_NAME).build();

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                .modelBiasBaselineConfig(resourceBaselineConfig)
                .roleArn(TEST_ARN)
                .build();

        final ResourceModel resourceModel = ResourceModel.builder()
                .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        verify(proxyClient.client(), times(1)).describeModelBiasJobDefinition(requestCaptor.capture());
        assertEquals(TEST_JOB_DEFINITION_NAME, requestCaptor.getValue().jobDefinitionName());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

        assertThat(response.getResourceModel()).isEqualTo(expectedResourceModel);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_ModelBiasJobDefinitionDoesNotExist_Fails() {
        final AwsServiceException resourceNotFoundException = AwsServiceException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class)))
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
        when(proxyClient.client().describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_JOB_DEFINITION_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                .build();
    }
}