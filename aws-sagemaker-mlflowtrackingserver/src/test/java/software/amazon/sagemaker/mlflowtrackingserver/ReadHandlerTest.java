package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.TrackingServerStatus;
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
        final DescribeMlflowTrackingServerResponse describeMlflowTrackingServerResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().tags(TEST_SDK_TAGS).build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(describeMlflowTrackingServerResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(true, false, false))
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
    public void testReadHandler_ResourceNotFoundException() {
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
                .desiredResourceState(createResourceModel(true, false, false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("InternalError")
                        .errorMessage("test error message")
                        .build())
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(true, false, false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                Action.READ));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
