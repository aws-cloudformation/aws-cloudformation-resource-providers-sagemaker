package software.amazon.sagemaker.userprofile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListUserProfilesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListUserProfilesResponse;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.UserProfileDetails;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends software.amazon.sagemaker.userprofile.AbstractTestBase {

    private static final String OPERATION = "SageMaker::ListUserProfiles";
    public static final String TEST_TOKEN = "testToken";

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
    public void testListHandler_SimpleSuccess() {
        final UserProfileDetails userProfile = UserProfileDetails.builder()
                .domainId(TEST_DOMAIN_ID)
                .userProfileName(TEST_USER_PROFILE_NAME)
                .build();

        final ListUserProfilesResponse listResponse = ListUserProfilesResponse.builder()
                .userProfiles(userProfile)
                .nextToken(TEST_TOKEN)
                .build();

        when(proxyClient.client().listUserProfiles(any(ListUserProfilesRequest.class)))
                .thenReturn(listResponse);

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .userProfileName(TEST_USER_PROFILE_NAME)
                .domainId(TEST_DOMAIN_ID)
                .build();

        List<ResourceModel> expectedModels = new ArrayList<ResourceModel>();
        expectedModels.add(expectedResourceModel);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(expectedModels);
        assertThat(response.getNextToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testListHandler_SimpleSuccess_NoUserProfilesExist() {
        final ListUserProfilesResponse listResponse = ListUserProfilesResponse.builder()
                .userProfiles(Collections.emptyList())
                .nextToken(null)
                .build();

        when(proxyClient.client().listUserProfiles(any(ListUserProfilesRequest.class)))
                .thenReturn(listResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(Collections.emptyList());
        assertThat(response.getNextToken()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testListHandler_ServiceInternalException() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("InternalError").errorMessage(OPERATION).build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        when(proxyClient.client().listUserProfiles(any(ListUserProfilesRequest.class)))
                .thenThrow(ex);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                OPERATION));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ListHandler handler = new ListHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}