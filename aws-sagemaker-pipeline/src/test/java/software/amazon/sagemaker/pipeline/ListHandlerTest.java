package software.amazon.sagemaker.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListPipelinesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListPipelinesResponse;
import software.amazon.awssdk.services.sagemaker.model.PipelineSummary;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {
    private final ResourceModel requestModel = getResourceModel(TEST_CFN_MODEL_TAGS_K1_V1);

    public static final String TEST_TOKEN = "test-token";

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
    public void testListHandler_SimpleSuccess() {
        final PipelineSummary pipelineSummary = PipelineSummary.builder()
                .pipelineName(TEST_PIPELINE_NAME)
                .pipelineDisplayName(TEST_PIPELINE_DISPLAY_NAME)
                .pipelineArn(TEST_PIPELINE_ARN)
                .pipelineDescription(TEST_PIPELINE_DESCRIPTION)
                .roleArn(TEST_ROLE_ARN)
                .build();

        final ListPipelinesResponse listPipelinesResponse =
                ListPipelinesResponse.builder()
                        .pipelineSummaries(pipelineSummary)
                        .nextToken(TEST_TOKEN)
                        .build();

        when(proxyClient.client().listPipelines(any(ListPipelinesRequest.class)))
                .thenReturn(listPipelinesResponse);

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .pipelineName(TEST_PIPELINE_NAME)
                .pipelineDisplayName(TEST_PIPELINE_DISPLAY_NAME)
                .pipelineDescription(TEST_PIPELINE_DESCRIPTION)
                .roleArn(TEST_ROLE_ARN)
                .build();

        List<ResourceModel> expectedModels = new ArrayList<ResourceModel>();
        expectedModels.add(expectedResourceModel);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(expectedModels);
        assertThat(response.getNextToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testListHandler_SimpleSuccess_NoPipelineExist() {
        final ListPipelinesResponse listPipelinesResponse =
                ListPipelinesResponse.builder()
                        .pipelineSummaries(Collections.emptyList())
                        .nextToken(null)
                        .build();

        when(proxyClient.client().listPipelines(any(ListPipelinesRequest.class)))
                .thenReturn(listPipelinesResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(Collections.emptyList());
        assertThat(response.getNextToken()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testListHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .errorCode("InternalError")
                        .build())
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        when(proxyClient.client().listPipelines(any(ListPipelinesRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                serviceInternalException.awsErrorDetails().errorMessage()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.pipeline.ListHandler handler = new ListHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
