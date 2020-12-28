package software.amazon.sagemaker.modelbiasjobdefinition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateModelBiasJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelBiasJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
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

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends software.amazon.sagemaker.modelbiasjobdefinition.AbstractTestBase {

    private final ResourceModel requestModel = ResourceModel.builder()
            .creationTime(TEST_TIME.toString())
            .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
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
        final DescribeModelBiasJobDefinitionResponse describeModelBiasJobDefinitionResponse =
                DescribeModelBiasJobDefinitionResponse.builder()
                        .creationTime(TEST_TIME)
                        .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                        .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                        .build();

        final CreateModelBiasJobDefinitionResponse createModelBiasJobDefinitionResponse = CreateModelBiasJobDefinitionResponse.builder()
                .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                .build();

        when(proxyClient.client().describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class)))
                .thenReturn(describeModelBiasJobDefinitionResponse);
        when(proxyClient.client().createModelBiasJobDefinition(any(CreateModelBiasJobDefinitionRequest.class)))
                .thenReturn(createModelBiasJobDefinitionResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_NoJobDefinitionName_Success() {
        final DescribeModelBiasJobDefinitionResponse describeModelBiasJobDefinitionResponse =
                DescribeModelBiasJobDefinitionResponse.builder()
                        .creationTime(TEST_TIME)
                        .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                        .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                        .build();

        final CreateModelBiasJobDefinitionResponse createModelBiasJobDefinitionResponse = CreateModelBiasJobDefinitionResponse.builder()
                .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                .build();

        when(proxyClient.client().describeModelBiasJobDefinition(any(DescribeModelBiasJobDefinitionRequest.class)))
                .thenReturn(describeModelBiasJobDefinitionResponse);
        when(proxyClient.client().createModelBiasJobDefinition(any(CreateModelBiasJobDefinitionRequest.class)))
                .thenReturn(createModelBiasJobDefinitionResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .clientRequestToken("token")
                .logicalResourceIdentifier("logical_id")
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_TIME.toString())
                .jobDefinitionName(TEST_JOB_DEFINITION_NAME)
                .jobDefinitionArn(TEST_JOB_DEFINITION_ARN)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().createModelBiasJobDefinition(any(CreateModelBiasJobDefinitionRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ModelBiasJobDefinitionAlreadyExists_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelBiasJobDefinition(any(CreateModelBiasJobDefinitionRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_JOB_DEFINITION_NAME));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelBiasJobDefinition(any(CreateModelBiasJobDefinitionRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsServiceException validationFailureException = SageMakerException.builder()
                .message("1 validation error detected: Value null at 'jobDefinitionName' " +
                            "failed to satisfy constraint: Member must not be null")
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelBiasJobDefinition(any(CreateModelBiasJobDefinitionRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelBiasJobDefinition(any(CreateModelBiasJobDefinitionRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}