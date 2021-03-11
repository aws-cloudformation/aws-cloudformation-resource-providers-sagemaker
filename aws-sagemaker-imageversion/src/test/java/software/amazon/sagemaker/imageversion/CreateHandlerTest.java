package software.amazon.sagemaker.imageversion;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageVersionStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
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
public class CreateHandlerTest extends AbstractTestBase {

    private static final String INVALID_CREATE_INPUTS_MESSAGE =
            "Invalid request provided: The following ReadOnly properties were set: " +
                    "[ImageVersionArn,Version,ContainerImage]";

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
                .baseImage(TEST_BASE_IMAGE)
                .build();
        invalidRequestModel = ResourceModel.builder()
                .imageName(TEST_IMAGE_NAME)
                .baseImage(TEST_BASE_IMAGE)
                .imageVersionArn(TEST_IMAGE_VERSION_ARN)
                .version(TEST_VERSION)
                .containerImage(TEST_CONTAINER_IMAGE)
                .build();
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
        final CreateImageVersionResponse createImageVersionResponse = CreateImageVersionResponse.builder()
                .imageVersionArn(TEST_IMAGE_VERSION_ARN)
                .build();
        final DescribeImageVersionResponse describeImageVersionResponse = createDescribeResponse(ImageVersionStatus.CREATED);

        when(proxyClient.client().createImageVersion(any(CreateImageVersionRequest.class)))
                .thenReturn(createImageVersionResponse);
        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenReturn(describeImageVersionResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_ResourceInUseException() {
        when(proxyClient.client().createImageVersion(any(CreateImageVersionRequest.class)))
                .thenThrow(ResourceInUseException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        assertThrows(CfnResourceConflictException.class, () -> invokeHandler(request));
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().createImageVersion(any(CreateImageVersionRequest.class)))
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

        when(proxyClient.client().createImageVersion(any(CreateImageVersionRequest.class)))
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
        assertThat(exception.getMessage()).isEqualTo(INVALID_CREATE_INPUTS_MESSAGE);
    }

    @Test
    public void testCreateHandler_VerifyStabilization_CreatedStatus() {
        final CreateImageVersionResponse createImageVersionResponse = CreateImageVersionResponse.builder()
                .imageVersionArn(TEST_IMAGE_VERSION_ARN)
                .build();
        final DescribeImageVersionResponse firstDescribeResponse = createDescribeResponse(ImageVersionStatus.CREATING);
        final DescribeImageVersionResponse secondDescribeResponse = createDescribeResponse(ImageVersionStatus.CREATED);

        when(proxyClient.client().createImageVersion(any(CreateImageVersionRequest.class)))
                .thenReturn(createImageVersionResponse);
        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel();

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
        final CreateImageVersionResponse createImageVersionResponse = CreateImageVersionResponse.builder()
                .imageVersionArn(TEST_IMAGE_VERSION_ARN)
                .build();
        final DescribeImageVersionResponse firstDescribeResponse = createDescribeResponse(ImageVersionStatus.CREATING);
        final DescribeImageVersionResponse secondDescribeResponse =
                createDescribeResponse(ImageVersionStatus.CREATE_FAILED);

        when(proxyClient.client().createImageVersion(any(CreateImageVersionRequest.class)))
                .thenReturn(createImageVersionResponse);
        when(proxyClient.client().describeImageVersion(any(DescribeImageVersionRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);

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
