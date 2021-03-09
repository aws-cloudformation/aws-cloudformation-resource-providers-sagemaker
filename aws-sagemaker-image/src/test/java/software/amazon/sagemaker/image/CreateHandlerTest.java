package software.amazon.sagemaker.image;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateImageRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateImageResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SageMakerClient> proxyClient;

    @Mock
    private SageMakerClient sageMakerClient;

    private ResourceModel createRequestModel;

    private ResourceModel invalidRequestModel;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sageMakerClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sageMakerClient);
        createRequestModel = ResourceModel.builder()
                .imageName(TEST_IMAGE_NAME)
                .imageRoleArn(TEST_IMAGE_ROLE_ARN)
                .imageDisplayName(TEST_IMAGE_DISPLAY_NAME)
                .imageDescription(TEST_IMAGE_DESCRIPTION)
                .tags(TEST_CFN_MODEL_TAGS)
                .build();
        invalidRequestModel = ResourceModel.builder()
                .imageArn(TEST_IMAGE_ARN)
                .imageName(TEST_IMAGE_NAME)
                .imageRoleArn(TEST_IMAGE_ROLE_ARN)
                .imageDisplayName(TEST_IMAGE_DISPLAY_NAME)
                .imageDescription(TEST_IMAGE_DESCRIPTION)
                .tags(TEST_CFN_MODEL_TAGS)
                .build();
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
        final CreateImageResponse createImageResponse = CreateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().createImage(any(CreateImageRequest.class)))
                .thenReturn(createImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class).thenReturn(describeImageResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(createRequestModel)
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
    public void testCreateHandler_ResourceAlreadyExists() {
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(createDescribeImageResponse(ImageStatus.CREATED));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_NAME));
    }

    @Test
    public void testCreateHandler_ResourceInUseException() {
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().createImage(any(CreateImageRequest.class)))
                .thenThrow(ResourceInUseException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_NAME));
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {

        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().createImage(any(CreateImageRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceeded = ResourceLimitExceededException.builder()
                .message("test error message")
                .statusCode(400)
                .build();

        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().createImage(any(CreateImageRequest.class)))
                .thenThrow(resourceLimitExceeded);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_InvalidRequestModel() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidRequestModel)
                .build();
        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).contains("Invalid request provided");
    }

    @Test
    public void testCreateHandler_VerifyStabilization_CreatedStatus() {
        final CreateImageResponse createImageResponse = CreateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse firstDescribeResponse = createDescribeImageResponse(ImageStatus.CREATING);
        final DescribeImageResponse secondDescribeResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().createImage(any(CreateImageRequest.class)))
                .thenReturn(createImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
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
    public void testCreateHandler_VerifyStabilization_CreateFailed() {
        final CreateImageResponse createImageResponse = CreateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse firstDescribeResponse = createDescribeImageResponse(ImageStatus.CREATING);
        final DescribeImageResponse secondDescribeResponse = createDescribeImageResponse(ImageStatus.CREATE_FAILED);

        when(proxyClient.client().createImage(any(CreateImageRequest.class)))
                .thenReturn(createImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        final CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
