package software.amazon.sagemaker.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AppStatus;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.ResourceSpec;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends software.amazon.sagemaker.app.AbstractTestBase {

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
    }

    @Test
    public void testDeleteHandler_SimpleSuccess() {
        final DescribeAppResponse describeAppResponse = DescribeAppResponse.builder()
                .status(AppStatus.IN_SERVICE)
                .build();

        final DeleteAppResponse deleteAppResponse = DeleteAppResponse.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(describeAppResponse).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteApp(any(DeleteAppRequest.class)))
                .thenReturn(deleteAppResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo((OperationStatus.SUCCESS));
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void testDeleteHandler_ResourceNotFound_DeletedApp() {
        final DescribeAppResponse describeAppResponse = DescribeAppResponse.builder()
                .status(AppStatus.DELETED)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(describeAppResponse);

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_APP_NAME));
    }

    @Test
    public void testDeleteHandler_ServiceInternalException() {
        final DescribeAppResponse describeAppResponse = DescribeAppResponse.builder()
                .status(AppStatus.IN_SERVICE)
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(describeAppResponse);

        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().deleteApp(any(DeleteAppRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.DELETE));
    }

    @Test
    public void testDeleteHandler_ResourceInUse_Fails() {
        final DescribeAppResponse describeAppResponse = DescribeAppResponse.builder()
                .status(AppStatus.IN_SERVICE)
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(describeAppResponse);
        when(proxyClient.client().deleteApp(any(DeleteAppRequest.class)))
                .thenThrow(ResourceInUseException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnResourceConflictException.class, () -> invokeHandleRequest(request));

        final String primaryIdentifier = String.format("%s|%s|%s|%s",
                TEST_APP_NAME,
                TEST_APP_TYPE,
                TEST_DOMAIN_ID,
                TEST_USER_PROFILE_NAME
        );

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ResourceConflict.getMessage(),
                ResourceModel.TYPE_NAME, primaryIdentifier, null));
    }

    @Test
    public void testDeleteHandler_ResourceDoesNotExists_Fails() {
        final DescribeAppResponse describeAppResponse = DescribeAppResponse.builder()
                        .status(AppStatus.IN_SERVICE)
                        .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(describeAppResponse);
        when(proxyClient.client().deleteApp(any(DeleteAppRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_APP_NAME));
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete() {
        final software.amazon.awssdk.services.sagemaker.model.ResourceSpec resourceSpec = ResourceSpec.builder()
                .instanceType(TEST_INSTANCE_TYPE)
                .sageMakerImageArn(TEST_IMAGE_ARN)
                .sageMakerImageVersionArn(TEST_IMAGE_VERSION_ARN)
                .build();

        final DescribeAppResponse firstDescribeResponse =
                DescribeAppResponse.builder()
                        .appArn(TEST_APP_ARN)
                        .appName(TEST_APP_NAME)
                        .appType(TEST_APP_TYPE)
                        .domainId(TEST_DOMAIN_ID)
                        .userProfileName(TEST_USER_PROFILE_NAME)
                        .resourceSpec(resourceSpec)
                        .status(AppStatus.DELETING)
                        .build();

        final DeleteAppResponse deleteAppResponse = DeleteAppResponse.builder()
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(firstDescribeResponse).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteApp(any(DeleteAppRequest.class)))
                .thenReturn(deleteAppResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDeletedStatus() {
        final DescribeAppResponse firstDescribeResponse =DescribeAppResponse.builder()
                .status(AppStatus.DELETING)
                .build();

        final DescribeAppResponse secondDescribeResponse = DescribeAppResponse.builder()
                .status(AppStatus.DELETED)
                .build();

        final DeleteAppResponse deleteAppResponse = DeleteAppResponse.builder()
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteApp(any(DeleteAppRequest.class)))
                .thenReturn(deleteAppResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_ResourceNotDeleted() {
        final DescribeAppResponse firstDescribeResponse =DescribeAppResponse.builder()
                        .status(AppStatus.DELETING)
                        .build();

        final DescribeAppResponse secondDescribeResponse = DescribeAppResponse.builder()
                .status(AppStatus.FAILED)
                        .build();

        final DeleteAppResponse deleteAppResponse = DeleteAppResponse.builder()
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteApp(any(DeleteAppRequest.class)))
                .thenReturn(deleteAppResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnNotStabilizedException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_APP_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
