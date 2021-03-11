package software.amazon.sagemaker.image;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

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
    public void testReadHandler_SimpleSuccess() {
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();

        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(ImageStatus.CREATED.toString(), true);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
                .build();
        final Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_NAME));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
                .build();
        final Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));
        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.GeneralServiceException.getMessage(), Action.READ));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
