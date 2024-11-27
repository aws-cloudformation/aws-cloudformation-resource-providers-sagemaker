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
import software.amazon.awssdk.services.sagemaker.model.CreatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.CreatePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ParallelismConfiguration;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    private final ResourceModel requestModel = getResourceModel(TEST_CFN_MODEL_TAGS_K1_V1);
    private ParallelismConfiguration parallelismConfiguration;

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
        parallelismConfiguration = ParallelismConfiguration.builder()
                .maxParallelExecutionSteps(2)
                .build();
    }

    @Test
    public void testCreateHandler_SimpleSuccess_PipelineDefinitionBody() {
        final DescribePipelineResponse describePipelineResponse =
                DescribePipelineResponse.builder()
                        .pipelineArn(TEST_PIPELINE_ARN)
                        .pipelineName(TEST_PIPELINE_NAME)
                        .pipelineDefinition(TEST_PIPELINE_DEFINITION)
                        .pipelineDescription(TEST_PIPELINE_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .pipelineDisplayName(TEST_PIPELINE_DISPLAY_NAME)
                        .creationTime(Instant.now())
                        .parallelismConfiguration(parallelismConfiguration)
                        .build();

        final CreatePipelineResponse createPipelineResponse = CreatePipelineResponse.builder()
                .pipelineArn(TEST_PIPELINE_ARN)
                .build();

        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(Tag.builder().key("key").value("value").build())
                .build();

        when(proxyClient.client().describePipeline(any(DescribePipelineRequest.class)))
                .thenReturn(describePipelineResponse);
        when(proxyClient.client().createPipeline(any(CreatePipelineRequest.class)))
                .thenReturn(createPipelineResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .pipelineName(TEST_PIPELINE_NAME)
                .pipelineDefinition(PipelineDefinition.builder()
                        .pipelineDefinitionBody(TEST_PIPELINE_DEFINITION).build())
                .pipelineDescription(TEST_PIPELINE_DESCRIPTION)
                .roleArn(TEST_ROLE_ARN)
                .pipelineDisplayName(TEST_PIPELINE_DISPLAY_NAME)
                .parallelismConfiguration(software.amazon.sagemaker.pipeline.ParallelismConfiguration.builder()
                        .maxParallelExecutionSteps(2).build())
                .tags(Collections.singletonList(new software.amazon.sagemaker.pipeline.Tag("value", "key")))
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
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("InternalError")
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .build())
                .statusCode(500)
                .build();

        when(proxyClient.client().createPipeline(any(CreatePipelineRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                serviceInternalException.awsErrorDetails().errorMessage()));
    }


    @Test
    public void testCreateHandler_PipelineAlreadyExists() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createPipeline(any(CreatePipelineRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createPipeline(any(CreatePipelineRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () -> invokeHandleRequest(request));
        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, TEST_ERROR_MESSAGE));
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsServiceException validationFailureException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ValidationException")
                        .errorMessage("Value null at 'pipelineName' failed to " +
                                "satisfy constraint: Member must not be null")
                        .build())
                .statusCode(400)
                .build();

        when(proxyClient.client().createPipeline(any(CreatePipelineRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                validationFailureException.awsErrorDetails().errorMessage()));
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createPipeline(any(CreatePipelineRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.pipeline.CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
