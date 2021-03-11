package software.amazon.sagemaker.userprofile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteUserProfileResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeUserProfileResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.UserProfileStatus;
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
public class DeleteHandlerTest extends software.amazon.sagemaker.userprofile.AbstractTestBase {

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
        final DeleteUserProfileResponse deleteUserProfileResponse = DeleteUserProfileResponse.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeUserProfile(any(DescribeUserProfileRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteUserProfile(any(DeleteUserProfileRequest.class)))
                .thenReturn(deleteUserProfileResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo((OperationStatus.SUCCESS));
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void testDeleteHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().deleteUserProfile(any(DeleteUserProfileRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.DELETE));
    }

    @Test
    public void testDeleteHandler_ResourceInUse_Fails() {
        when(proxyClient.client().deleteUserProfile(any(DeleteUserProfileRequest.class)))
                .thenThrow(ResourceInUseException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnResourceConflictException.class, () -> invokeHandleRequest(request));

        final String primaryIdentifier = String.format("%s|%s", TEST_DOMAIN_ID, TEST_USER_PROFILE_NAME);

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ResourceConflict.getMessage(),
                ResourceModel.TYPE_NAME, primaryIdentifier, null));
    }

    @Test
    public void testDeleteHandler_ResourceDoesNotExists_Fails() {
        when(proxyClient.client().deleteUserProfile(any(DeleteUserProfileRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_USER_PROFILE_NAME));
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete() {
        final DescribeUserProfileResponse firstDescribeResponse =
                DescribeUserProfileResponse.builder()
                        .userProfileName(TEST_USER_PROFILE_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .creationTime(TEST_TIME)
                        .lastModifiedTime(TEST_TIME)
                        .status(UserProfileStatus.DELETING)
                        .build();

        final DeleteUserProfileResponse deleteUserProfileResponse = DeleteUserProfileResponse.builder()
                .build();

        when(proxyClient.client().describeUserProfile(any(DescribeUserProfileRequest.class)))
                .thenReturn(firstDescribeResponse).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteUserProfile(any(DeleteUserProfileRequest.class)))
                .thenReturn(deleteUserProfileResponse);

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
        final DescribeUserProfileResponse firstDescribeResponse =DescribeUserProfileResponse.builder()
                        .status(UserProfileStatus.PENDING)
                        .build();

        final DescribeUserProfileResponse secondDescribeResponse = DescribeUserProfileResponse.builder()
                .status(UserProfileStatus.FAILED)
                        .build();

        final DeleteUserProfileResponse deleteUserProfileResponse = DeleteUserProfileResponse.builder()
                .build();

        when(proxyClient.client().describeUserProfile(any(DescribeUserProfileRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteUserProfile(any(DeleteUserProfileRequest.class)))
                .thenReturn(deleteUserProfileResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnNotStabilizedException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_USER_PROFILE_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
