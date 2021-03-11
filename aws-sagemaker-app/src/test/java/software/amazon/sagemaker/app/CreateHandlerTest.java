package software.amazon.sagemaker.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AppStatus;
import software.amazon.awssdk.services.sagemaker.model.CreateAppRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateAppResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
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
public class CreateHandlerTest extends software.amazon.sagemaker.app.AbstractTestBase {

    private final ResourceSpec RESOURCE_SPEC = ResourceSpec.builder()
            .instanceType(TEST_INSTANCE_TYPE)
            .sageMakerImageArn(TEST_IMAGE_ARN)
            .sageMakerImageVersionArn(TEST_IMAGE_VERSION_ARN)
            .build();

    private final ResourceModel REQUEST_MODEL = ResourceModel.builder()
            .userProfileName(TEST_USER_PROFILE_NAME)
            .resourceSpec(RESOURCE_SPEC)
            .domainId(TEST_DOMAIN_ID)
            .appType(TEST_APP_TYPE)
            .appName(TEST_APP_NAME)
            .build();

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
    public void testCreateHandler_SimpleSuccess() {
        final software.amazon.awssdk.services.sagemaker.model.ResourceSpec resourceSpec =
                software.amazon.awssdk.services.sagemaker.model.ResourceSpec.builder()
                        .instanceType(TEST_INSTANCE_TYPE)
                        .sageMakerImageArn(TEST_IMAGE_ARN)
                        .sageMakerImageVersionArn(TEST_IMAGE_VERSION_ARN)
                        .build();

        final DescribeAppResponse describeAppResponse =
                DescribeAppResponse.builder()
                        .appArn(TEST_APP_ARN)
                        .appName(TEST_APP_NAME)
                        .appType(TEST_APP_TYPE)
                        .domainId(TEST_DOMAIN_ID)
                        .userProfileName(TEST_USER_PROFILE_NAME)
                        .resourceSpec(resourceSpec)
                        .status(AppStatus.IN_SERVICE)
                        .build();

        final CreateAppResponse createResponse = CreateAppResponse.builder()
                .appArn(TEST_APP_ARN)
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(describeAppResponse);
        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .appArn(TEST_APP_ARN)
                .appName(TEST_APP_NAME)
                .appType(TEST_APP_TYPE)
                .domainId(TEST_DOMAIN_ID)
                .userProfileName(TEST_USER_PROFILE_NAME)
                .resourceSpec(RESOURCE_SPEC)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_InvalidRequestArn() {
        final ResourceModel invalidModel = ResourceModel.builder()
                .appArn(TEST_APP_ARN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "The following property 'AppArn' is not allowed to configured."));
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ResourceAlreadyExists_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_APP_NAME));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, TEST_ERROR_MESSAGE));
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsErrorDetails awsErrorDetails =
                AwsErrorDetails.builder().errorCode("ValidationError").errorMessage(TEST_ERROR_MESSAGE).build();

        final AwsServiceException validationFailureException = SageMakerException.builder()
                .awsErrorDetails(awsErrorDetails)
                .build();

        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                TEST_ERROR_MESSAGE));
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_InService() {
        final software.amazon.awssdk.services.sagemaker.model.ResourceSpec resourceSpec =
                software.amazon.awssdk.services.sagemaker.model.ResourceSpec.builder()
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
                        .status(AppStatus.PENDING)
                        .build();

        final DescribeAppResponse secondDescribeResponse =
                DescribeAppResponse.builder()
                        .appArn(TEST_APP_ARN)
                        .appName(TEST_APP_NAME)
                        .appType(TEST_APP_TYPE)
                        .domainId(TEST_DOMAIN_ID)
                        .userProfileName(TEST_USER_PROFILE_NAME)
                        .resourceSpec(resourceSpec)
                        .status(AppStatus.IN_SERVICE)
                        .build();

        final CreateAppResponse createAppResponse = CreateAppResponse.builder()
                .appArn(TEST_APP_ARN)
                .build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenReturn(createAppResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .appArn(TEST_APP_ARN)
                .userProfileName(TEST_USER_PROFILE_NAME)
                .resourceSpec(RESOURCE_SPEC)
                .domainId(TEST_DOMAIN_ID)
                .appType(TEST_APP_TYPE)
                .appName(TEST_APP_NAME)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_VerifyStabilization_Failed() {
        final DescribeAppResponse firstDescribeResponse =
                DescribeAppResponse.builder()
                        .status(AppStatus.PENDING)
                        .build();

        final DescribeAppResponse secondDescribeResponse =
                DescribeAppResponse.builder()
                        .status(AppStatus.FAILED)
                        .build();

        final CreateAppResponse createAppResponse = CreateAppResponse.builder().build();

        when(proxyClient.client().describeApp(any(DescribeAppRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createApp(any(CreateAppRequest.class)))
                .thenReturn(createAppResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).
                isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(), ResourceModel.TYPE_NAME, TEST_APP_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}