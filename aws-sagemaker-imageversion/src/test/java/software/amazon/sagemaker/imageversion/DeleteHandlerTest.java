package software.amazon.sagemaker.imageversion;

import java.time.Duration;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageVersionStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SageMakerClient> proxyClient;

    @Mock
    private SageMakerClient sageMakerClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sageMakerClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sageMakerClient);
    }

    @Test
    public void testDeleteHandler_SimpleSuccess() {
        final DeleteImageVersionResponse deleteImageVersionResponse = DeleteImageVersionResponse.builder().build();
        final DescribeImageVersionResponse describeImageVersionResponse =
                createDescribeResponse(ImageVersionStatus.CREATED);

        when(proxyClient.client().deleteImageVersion(any(DeleteImageVersionRequest.class)))
                .thenReturn(deleteImageVersionResponse);
        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenReturn(describeImageVersionResponse)
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_ResourceAlreadyDeleting() {
        final DescribeImageVersionResponse describeImageVersionResponse =
                createDescribeResponse(ImageVersionStatus.DELETING);

        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenReturn(describeImageVersionResponse)
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_ResourceAlreadyDeleted() {
        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel())
                .build();
        final Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandler(request));

        assertThat(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_VERSION_ARN)).isEqualTo(exception.getMessage());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete() {
        final DeleteImageVersionResponse deleteImageVersionResponse = DeleteImageVersionResponse.builder().build();
        final DescribeImageVersionResponse createdResponse = createDescribeResponse(ImageVersionStatus.CREATED);
        final DescribeImageVersionResponse deletingResponse = createDescribeResponse(ImageVersionStatus.DELETING);

        when(proxyClient.client().deleteImageVersion(any(DeleteImageVersionRequest.class)))
                .thenReturn(deleteImageVersionResponse);
        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenReturn(createdResponse) // existing state
                .thenReturn(deletingResponse) // delete started
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_DeleteFailed() {
        final DeleteImageVersionResponse deleteImageVersionResponse = DeleteImageVersionResponse.builder().build();
        final DescribeImageVersionResponse createdResponse = createDescribeResponse(ImageVersionStatus.CREATED);
        final DescribeImageVersionResponse deletingResponse = createDescribeResponse(ImageVersionStatus.DELETING);
        final DescribeImageVersionResponse deleteFailedResponse =
                createDescribeResponse(ImageVersionStatus.DELETE_FAILED);

        when(proxyClient.client().deleteImageVersion(any(DeleteImageVersionRequest.class)))
                .thenReturn(deleteImageVersionResponse);
        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenReturn(createdResponse)
                .thenReturn(deletingResponse)
                .thenReturn(deleteFailedResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel())
                .build();
        final Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandler(request));

        assertThat(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_VERSION_ARN)).isEqualTo(exception.getMessage());
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
