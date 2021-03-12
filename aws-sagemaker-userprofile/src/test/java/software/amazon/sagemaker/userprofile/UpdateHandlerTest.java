package software.amazon.sagemaker.userprofile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeUserProfileResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.UpdateUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateUserProfileResponse;
import software.amazon.awssdk.services.sagemaker.model.UserProfileStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
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
public class UpdateHandlerTest extends software.amazon.sagemaker.userprofile.AbstractTestBase {

    private static final String OPERATION = "SageMaker::UpdateUserProfile";

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
    public void testUpdateHandler_SimpleSuccess() {
        final DescribeUserProfileResponse describeUserProfileResponse =
                DescribeUserProfileResponse.builder()
                        .userProfileName(TEST_USER_PROFILE_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .creationTime(TEST_TIME)
                        .lastModifiedTime(TEST_TIME)
                        .status(UserProfileStatus.IN_SERVICE)
                        .build();

        final UpdateUserProfileResponse updateUserProfileResponse =
                UpdateUserProfileResponse
                        .builder()
                        .userProfileArn(TEST_USER_PROFILE_ARN)
                        .build();

        when(proxyClient.client().describeUserProfile(any(DescribeUserProfileRequest.class)))
                .thenReturn(describeUserProfileResponse);
        when(proxyClient.client().updateUserProfile(any(UpdateUserProfileRequest.class)))
                .thenReturn(updateUserProfileResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .userProfileArn(TEST_USER_PROFILE_ARN)
                .userProfileName(TEST_USER_PROFILE_NAME)
                .domainId(TEST_DOMAIN_ID)
                .singleSignOnUserValue(TEST_SSO_VALUE)
                .singleSignOnUserIdentifier(TEST_SSO_ID)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().updateUserProfile(any(UpdateUserProfileRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.UPDATE.toString()));
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException() {
        when(proxyClient.client().updateUserProfile(any(UpdateUserProfileRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_USER_PROFILE_NAME));
    }

    @Test
    public void testUpdateHandler_ResourceLimitExceededException() {
        when(proxyClient.client().updateUserProfile(any(UpdateUserProfileRequest.class)))
                .thenThrow(ResourceLimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, null));
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_SuccessfulUpdate() {
        final DescribeUserProfileResponse firstDescribeResponse =
                DescribeUserProfileResponse.builder()
                        .status(UserProfileStatus.UPDATING)
                        .build();

        final DescribeUserProfileResponse secondDescribeResponse =
                DescribeUserProfileResponse.builder()
                        .status(UserProfileStatus.IN_SERVICE)
                        .build();

        final UpdateUserProfileResponse updateUserProfileResponse =
                UpdateUserProfileResponse.builder()
                        .userProfileArn(TEST_USER_PROFILE_ARN)
                        .build();

        when(proxyClient.client().describeUserProfile(any(DescribeUserProfileRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateUserProfile(any(UpdateUserProfileRequest.class)))
                .thenReturn(updateUserProfileResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .userProfileArn(TEST_USER_PROFILE_ARN)
                .userProfileName(TEST_USER_PROFILE_NAME)
                .domainId(TEST_DOMAIN_ID)
                .singleSignOnUserValue(TEST_SSO_VALUE)
                .singleSignOnUserIdentifier(TEST_SSO_ID)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void  testUpdateHandler_VerifyStabilization_Failed() {
        final DescribeUserProfileResponse firstDescribeResponse =
                DescribeUserProfileResponse.builder()
                        .userProfileName(TEST_USER_PROFILE_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .status(UserProfileStatus.UPDATING)
                        .build();

        final DescribeUserProfileResponse secondDescribeResponse =
                DescribeUserProfileResponse.builder()
                        .userProfileName(TEST_USER_PROFILE_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .status(UserProfileStatus.UPDATE_FAILED)
                        .build();

        final UpdateUserProfileResponse updateUserProfileResponse = UpdateUserProfileResponse.builder()
                .build();

        when(proxyClient.client().describeUserProfile(any(DescribeUserProfileRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateUserProfile(any(UpdateUserProfileRequest.class)))
                .thenReturn(updateUserProfileResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during update of " + getRequestResourceModel().getPrimaryIdentifier()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}