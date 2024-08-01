package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.TrackingServerStatus;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
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

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sageMakerClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sageMakerClient);
        createRequestModel = ResourceModel.builder()
                .trackingServerName(TEST_TRACKING_SERVER_NAME)
                .roleArn(TEST_DEFAULT_ROLE_ARN)
                .artifactStoreUri(TEST_DEFAULT_ARTIFACT_STORE_URI)
                .tags(TEST_CFN_MODEL_TAGS)
                .build();
    }

    @Test
    public void testCreateHandler() {
        assertThat(new CreateHandler()).isNotNull();
        assertThat(new CreateHandler(TEST_BACKOFF_STRATEGY)).isNotNull();
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
        final CreateMlflowTrackingServerResponse createMlflowTrackingServerResponse = CreateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse describeMlflowTrackingServerResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenReturn(createMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(describeMlflowTrackingServerResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(true, false, false);

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
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME));
    }

    @Test
    public void testCreateHandler_ResourceInUseException() {
        final String errorCode = "ResourceInUseException";
        final ResourceInUseException resourceInUse = ResourceInUseException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage("test error message")
                        .sdkHttpResponse(SdkHttpResponse.builder().statusCode(400).build())
                        .build())
                .message("test error message")
                .statusCode(400)
                .build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenThrow(resourceInUse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME));
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {

        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("InternalError")
                        .errorMessage("test error message")
                        .build())
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final String errorCode = "ResourceLimitExceeded";
        final ResourceLimitExceededException resourceLimitExceeded = ResourceLimitExceededException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage("test error message")
                        .build())
                .message("test error message")
                .statusCode(400)
                .build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenThrow(resourceLimitExceeded);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceLimitExceeded);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, errorCode));
    }

    @Test
    public void testCreateHandler_TaggingAccessDeniedException() {
        final String errorCode = "AccessDeniedException";
        final AwsServiceException accessDeniedException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage("test error message")
                        .build())
                .message("test error message")
                .statusCode(500)
                .build();
        final CreateMlflowTrackingServerResponse createMlflowTrackingServerResponse = CreateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse describeMlflowTrackingServerResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(describeMlflowTrackingServerResponse);
        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenReturn(createMlflowTrackingServerResponse);
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenThrow(accessDeniedException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AccessDenied);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.AccessDenied.getMessage(),
                Action.CREATE, errorCode));
    }

    @Test
    public void testCreateHandler_StabilizationFailed_UnknownStatus() {
        final CreateMlflowTrackingServerResponse createMlflowTrackingServerResponse = CreateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse unknownDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UNKNOWN_TO_SDK_VERSION, false);

        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenReturn(createMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(unknownDescribeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();

        final Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                String.format("Stabilizing of %s failed with an unexpected status %s",
                        TEST_TRACKING_SERVER_ARN, TrackingServerStatus.UNKNOWN_TO_SDK_VERSION)));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_CreatedStatus() {
        final CreateMlflowTrackingServerResponse createMlflowTrackingServerResponse = CreateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse creatingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATING, false);
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenReturn(createMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(creatingDescribeResponse)
                .thenReturn(createdDescribeResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(true, false, false);

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
        final CreateMlflowTrackingServerResponse createMlflowTrackingServerResponse = CreateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse creatingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATING, false);
        final DescribeMlflowTrackingServerResponse createFailedDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATE_FAILED, false);

        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenReturn(createMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(creatingDescribeResponse)
                .thenReturn(createFailedDescribeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();
        final Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandler(request));
        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_ARN));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_ThrottlingErrors() {
        final CreateMlflowTrackingServerResponse createMlflowTrackingServerResponse = CreateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse creatingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATING, false);
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        final Exception rateLimitExceeded = SageMakerException.builder()
                .message("Rate limit exceeded")
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ThrottlingException")
                        .errorMessage("Rate limit exceeded")
                        .build())
                .build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenThrow(rateLimitExceeded);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createRequestModel)
                .build();

        // Create a single shared CreateHandler
        final CreateHandler handler = new CreateHandler(TEST_BACKOFF_STRATEGY);
        // Create a single shared callbackContext and use it to call the CreateHandler's handleRequest repeatedly.
        final CallbackContext callbackContext = new CallbackContext();

        final ProgressEvent<ResourceModel, CallbackContext> firstAttemptResponse =
                invokeHandler(request, handler, callbackContext);

        assertThat(firstAttemptResponse).isNotNull();
        assertThat(firstAttemptResponse.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(firstAttemptResponse.getErrorCode()).isEqualTo(HandlerErrorCode.Throttling);
        assertThat(firstAttemptResponse.getMessage()).isNull();

        // Make the second attempt without throttling
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(creatingDescribeResponse)
                .thenReturn(createdDescribeResponse);
        when(proxyClient.client().createMlflowTrackingServer(any(CreateMlflowTrackingServerRequest.class)))
                .thenReturn(createMlflowTrackingServerResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> secondAttemptResponse =
                invokeHandler(request, handler, firstAttemptResponse.getCallbackContext());

        final ResourceModel expectedModelFromResponse = createResourceModel(true, false, false);

        assertThat(secondAttemptResponse).isNotNull();
        assertThat(secondAttemptResponse.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(secondAttemptResponse.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(secondAttemptResponse.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(secondAttemptResponse.getResourceModels()).isNull();
        assertThat(secondAttemptResponse.getMessage()).isNull();
        assertThat(secondAttemptResponse.getErrorCode()).isNull();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        return invokeHandler(request, new CreateHandler(TEST_BACKOFF_STRATEGY), new CallbackContext());
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(
            final ResourceHandlerRequest<ResourceModel> request, final CreateHandler handler, final CallbackContext callbackContext) {
        return handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);
    }
}
