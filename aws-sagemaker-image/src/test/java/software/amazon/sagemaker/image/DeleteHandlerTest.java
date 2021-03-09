package software.amazon.sagemaker.image;

import java.time.Duration;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        final DeleteImageResponse deleteImageResponse = DeleteImageResponse.builder().build();
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);

        when(proxyClient.client().deleteImage(any(DeleteImageRequest.class)))
                .thenReturn(deleteImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse)
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), false))
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
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.DELETING);

        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse)
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), false))
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
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), false))
                .build();
        final Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandler(request));

        assertThat(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_NAME)).isEqualTo(exception.getMessage());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete() {
        final DeleteImageResponse deleteImageResponse = DeleteImageResponse.builder().build();
        final DescribeImageResponse createdImageResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final DescribeImageResponse deletingImageResponse = createDescribeImageResponse(ImageStatus.DELETING);

        when(proxyClient.client().deleteImage(any(DeleteImageRequest.class)))
                .thenReturn(deleteImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(createdImageResponse) // existing state
                .thenReturn(deletingImageResponse) // delete started
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), false))
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
        final DeleteImageResponse deleteImageResponse = DeleteImageResponse.builder().build();
        final DescribeImageResponse createdImageResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final DescribeImageResponse deletingResponse = createDescribeImageResponse(ImageStatus.DELETING);
        final DescribeImageResponse deleteFailedResponse = createDescribeImageResponse(ImageStatus.DELETE_FAILED);

        when(proxyClient.client().deleteImage(any(DeleteImageRequest.class)))
                .thenReturn(deleteImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(createdImageResponse)
                .thenReturn(deletingResponse)
                .thenReturn(deleteFailedResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), false))
                .build();
        final Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandler(request));

        assertThat(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_NAME)).isEqualTo(exception.getMessage());
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
