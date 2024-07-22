package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.*;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
    public void testDeleteHandler() {
        assertThat(new DeleteHandler()).isNotNull();
        assertThat(new DeleteHandler(TEST_BACKOFF_STRATEGY)).isNotNull();
    }

    @Test
    public void testDeleteHandler_SimpleSuccess() {
        final DeleteMlflowTrackingServerResponse deleteMlflowTrackingServerResponse = DeleteMlflowTrackingServerResponse.builder().build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenReturn(deleteMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse)
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
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
    public void testDeleteHandler_ResourceInUseException() {
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

        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse);
        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenThrow(resourceInUse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ResourceConflict);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.ResourceConflict.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME, errorCode));
    }

    @Test
    public void testDeleteHandler_ServiceInternalException() {
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("InternalError")
                        .errorMessage("test error message")
                        .build())
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse);
        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                Action.DELETE));
    }

    @Test
    public void testDeleteHandler_StabilizationFailed_UnknownStatus() {
        final DeleteMlflowTrackingServerResponse deleteMlflowTrackingServerResponse = DeleteMlflowTrackingServerResponse.builder().build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse unknownDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UNKNOWN_TO_SDK_VERSION, false);

        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenReturn(deleteMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse)
                .thenReturn(unknownDescribeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
                .build();

        final Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                String.format("Unexpected status: [%s] while stabilizing delete for resource: [%s]",
                        TrackingServerStatus.UNKNOWN_TO_SDK_VERSION, TEST_TRACKING_SERVER_ARN)));
    }

    @Test
    public void testDeleteHandler_VerifyStabilizationResourceNotFound_ResourceAlreadyDeleting() {
        // Wait for delete and stabilization at ResourceNotFoundException.
        final DescribeMlflowTrackingServerResponse deletingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.DELETING, false);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(deletingDescribeResponse)
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
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
        final String errorCode = "ResourceNotFound";
        final ResourceNotFoundException resourceNotFound = ResourceNotFoundException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage("test error message")
                        .sdkHttpResponse(SdkHttpResponse.builder().statusCode(400).build())
                        .build())
                .message("test error message")
                .statusCode(400)
                .build();
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(resourceNotFound);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME));
    }

    @Test
    public void testDeleteHandler_VerifyStabilizationResourceNotFound_SuccessfulDelete() {
        // Successful Delete with stabilization at ResourceNotFoundException.
        final DeleteMlflowTrackingServerResponse deleteMlflowTrackingServerResponse = DeleteMlflowTrackingServerResponse.builder().build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse deletingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.DELETING, false);

        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenReturn(deleteMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse) // existing state
                .thenReturn(deletingDescribeResponse) // delete started
                .thenThrow(ResourceNotFoundException.class); // delete complete

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
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
        final DeleteMlflowTrackingServerResponse deleteMlflowTrackingServerResponse = DeleteMlflowTrackingServerResponse.builder().build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse deletingResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.DELETING, false);
        final DescribeMlflowTrackingServerResponse deleteFailedResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.DELETE_FAILED, false);

        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenReturn(deleteMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse)
                .thenReturn(deletingResponse)
                .thenReturn(deleteFailedResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        final Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandler(request));

        assertThat(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME)).isEqualTo(exception.getMessage());
    }

    @Test
    public void testCreateHandler_VerifyStabilization_ThrottlingErrors() {
        final DeleteMlflowTrackingServerResponse deleteMlflowTrackingServerResponse = DeleteMlflowTrackingServerResponse.builder().build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse deletingResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.DELETING, false);

        final Exception rateLimitExceeded = SageMakerException.builder()
                .message("Rate limit exceeded")
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ThrottlingException")
                        .errorMessage("Rate limit exceeded")
                        .build())
                .build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse); // existing state
        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenThrow(rateLimitExceeded);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(false, false, false))
                .build();

        // Create a single shared DeleteHandler
        final DeleteHandler handler = new DeleteHandler(TEST_BACKOFF_STRATEGY);
        // Create a single shared callbackContext and use it to call the DeleteHandler's handleRequest repeatedly.
        final CallbackContext callbackContext = new CallbackContext();

        final ProgressEvent<ResourceModel, CallbackContext> firstAttemptResponse =
                invokeHandler(request, handler, callbackContext);

        assertThat(firstAttemptResponse).isNotNull();
        assertThat(firstAttemptResponse.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(firstAttemptResponse.getErrorCode()).isEqualTo(HandlerErrorCode.Throttling);
        assertThat(firstAttemptResponse.getMessage()).isNull();

        // Make the second attempt without throttling
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse) // existing state
                .thenReturn(deletingResponse) // delete started
                .thenThrow(ResourceNotFoundException.class); // delete complete
        when(proxyClient.client().deleteMlflowTrackingServer(any(DeleteMlflowTrackingServerRequest.class)))
                .thenReturn(deleteMlflowTrackingServerResponse);

        final ProgressEvent<ResourceModel, CallbackContext> secondAttemptResponse =
                invokeHandler(request, handler, firstAttemptResponse.getCallbackContext());

        assertThat(secondAttemptResponse).isNotNull();
        assertThat(secondAttemptResponse.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(secondAttemptResponse.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(secondAttemptResponse.getResourceModel()).isNull();
        assertThat(secondAttemptResponse.getResourceModels()).isNull();
        assertThat(secondAttemptResponse.getMessage()).isNull();
        assertThat(secondAttemptResponse.getErrorCode()).isNull();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        return invokeHandler(request, new DeleteHandler(TEST_BACKOFF_STRATEGY), new CallbackContext());
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request, final DeleteHandler handler, CallbackContext callbackContext) {
        return handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);
    }
}
