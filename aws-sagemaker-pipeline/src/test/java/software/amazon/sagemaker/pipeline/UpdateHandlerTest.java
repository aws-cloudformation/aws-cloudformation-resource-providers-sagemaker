package software.amazon.sagemaker.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.AddTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.ParallelismConfiguration;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

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
    public void testUpdateHandler_SimpleSuccess_NoTags() {
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

        final UpdatePipelineResponse updatePipelineResponse = UpdatePipelineResponse.builder()
                .pipelineArn(TEST_PIPELINE_ARN)
                .build();

        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();

        when(proxyClient.client().describePipeline(any(DescribePipelineRequest.class)))
                .thenReturn(describePipelineResponse);
        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenReturn(updatePipelineResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getResourceModel(Collections.emptyList()))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResourceModel(Collections.emptyList());

        verify(proxyClient.client(), times(0)).addTags(any(AddTagsRequest.class));
        verify(proxyClient.client(), times(0)).deleteTags(any(DeleteTagsRequest.class));
        verify(proxyClient.client(), times(2)).listTags(any(ListTagsRequest.class));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_AddTags() {
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

        final UpdatePipelineResponse updatePipelineResponse = UpdatePipelineResponse.builder()
                .pipelineArn(TEST_PIPELINE_ARN)
                .build();

        final ListTagsResponse listTagsResponseWithoutTags = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();
        final ListTagsResponse listTagsResponseWithTags = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS_K1_V1)
                .build();
        final AddTagsResponse addTagsResponse = AddTagsResponse.builder()
                .tags(TEST_SDK_TAGS_K1_V1)
                .build();

        when(proxyClient.client().describePipeline(any(DescribePipelineRequest.class)))
                .thenReturn(describePipelineResponse);
        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenReturn(updatePipelineResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithoutTags).thenReturn(listTagsResponseWithTags);
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenReturn(addTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getResourceModel(TEST_CFN_MODEL_TAGS_K1_V1))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResourceModel(TEST_CFN_MODEL_TAGS_K1_V1);

        verify(proxyClient.client(), times(0)).deleteTags(any(DeleteTagsRequest.class));
        verify(proxyClient.client(), times(1)).addTags(any(AddTagsRequest.class));
        verify(proxyClient.client(), times(2)).listTags(any(ListTagsRequest.class));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_AddTags_ExistingKey() {
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

        final UpdatePipelineResponse updatePipelineResponse = UpdatePipelineResponse.builder()
                .pipelineArn(TEST_PIPELINE_ARN)
                .build();

        final ListTagsResponse listTagsResponseWithInitialTags = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS_K1_V1)
                .build();
        final ListTagsResponse listTagsResponseWithUpdatedTags = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS_K1_V2)
                .build();
        final AddTagsResponse addTagsResponse = AddTagsResponse.builder()
                .tags(TEST_SDK_TAGS_K1_V2)
                .build();

        when(proxyClient.client().describePipeline(any(DescribePipelineRequest.class)))
                .thenReturn(describePipelineResponse);
        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenReturn(updatePipelineResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithInitialTags).thenReturn(listTagsResponseWithUpdatedTags);
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenReturn(addTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getResourceModel(TEST_CFN_MODEL_TAGS_K1_V2))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResourceModel(TEST_CFN_MODEL_TAGS_K1_V2);

        verify(proxyClient.client(), times(1)).addTags(any(AddTagsRequest.class));
        verify(proxyClient.client(), times(0)).deleteTags(any(DeleteTagsRequest.class));
        verify(proxyClient.client(), times(2)).listTags(any(ListTagsRequest.class));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_RemoveTags() {
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

        final UpdatePipelineResponse updatePipelineResponse = UpdatePipelineResponse.builder()
                .pipelineArn(TEST_PIPELINE_ARN)
                .build();

        final ListTagsResponse listTagsResponseWithoutTags = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();
        final ListTagsResponse listTagsResponseWithTags = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS_K1_V1)
                .build();
        final DeleteTagsResponse deleteTagsResponse = DeleteTagsResponse.builder()
                .build();

        when(proxyClient.client().describePipeline(any(DescribePipelineRequest.class)))
                .thenReturn(describePipelineResponse);
        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenReturn(updatePipelineResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithTags).thenReturn(listTagsResponseWithoutTags);
        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenReturn(deleteTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getResourceModel(Collections.emptyList()))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResourceModel(Collections.emptyList());

        verify(proxyClient.client(), times(0)).addTags(any(AddTagsRequest.class));
        verify(proxyClient.client(), times(1)).deleteTags(any(DeleteTagsRequest.class));
        verify(proxyClient.client(), times(2)).listTags(any(ListTagsRequest.class));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_PipelineAlreadyExists() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getResourceModel(TEST_CFN_MODEL_TAGS_K1_V1))
                .build();

        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME));
    }

    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getResourceModel(TEST_CFN_MODEL_TAGS_K1_V1))
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_updateTags() {
        final UpdatePipelineResponse updatePipelineResponse = UpdatePipelineResponse.builder()
                .pipelineArn(TEST_PIPELINE_ARN)
                .build();

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

        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenReturn(updatePipelineResponse).thenReturn(updatePipelineResponse);
        when(proxyClient.client().describePipeline(any(DescribePipelineRequest.class)))
                .thenReturn(describePipelineResponse).thenReturn(describePipelineResponse);

        // Test invalid null key
        ArrayList<software.amazon.sagemaker.pipeline.Tag> tags1 = new ArrayList<>();
        software.amazon.sagemaker.pipeline.Tag nullKeyTag = software.amazon.sagemaker.pipeline.Tag.builder().key(null).value("value").build();
        tags1.add(nullKeyTag);
        ResourceModel model1 = getResourceModel(tags1);

        final ResourceHandlerRequest<ResourceModel> request1 = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model1)
                .build();

        Exception exception1 = assertThrows(CfnInvalidRequestException.class, () -> invokeHandleRequest(request1));
        assertThat(exception1.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "Tag cannot have a null key"));

        // Test invalid null value
        ArrayList<software.amazon.sagemaker.pipeline.Tag> tags2 = new ArrayList<>();
        software.amazon.sagemaker.pipeline.Tag nullValueTag = software.amazon.sagemaker.pipeline.Tag.builder().key("key").value(null).build();
        tags2.add(nullValueTag);
        ResourceModel model2 = getResourceModel(tags2);

        final ResourceHandlerRequest<ResourceModel> request2 = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model2)
                .build();

        Exception exception2 = assertThrows(CfnInvalidRequestException.class, () -> invokeHandleRequest(request2));
        assertThat(exception2.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "Tag cannot have a null value for key: key"));
    }

    @Test
    public void testUpdateHandler_ServiceException_updateTags() {
        final UpdatePipelineResponse updatePipelineResponse = UpdatePipelineResponse.builder()
                .pipelineArn(TEST_PIPELINE_ARN)
                .build();

        final ResourceNotFoundException resourceNotFoundException = ResourceNotFoundException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(404)
                .build();

        when(proxyClient.client().updatePipeline(any(UpdatePipelineRequest.class)))
                .thenReturn(updatePipelineResponse).thenReturn(updatePipelineResponse);
        when(proxyClient.client().describePipeline(any(DescribePipelineRequest.class)))
                .thenThrow(resourceNotFoundException);


        ArrayList<software.amazon.sagemaker.pipeline.Tag> tags = new ArrayList<>();
        ResourceModel model = getResourceModel(tags);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Exception exception1 = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));
        assertThat(exception1.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.pipeline.UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
