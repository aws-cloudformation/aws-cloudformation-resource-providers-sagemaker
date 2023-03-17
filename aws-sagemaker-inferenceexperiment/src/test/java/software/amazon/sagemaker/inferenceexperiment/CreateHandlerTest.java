package software.amazon.sagemaker.inferenceexperiment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.utils.DateUtils;
import software.amazon.awssdk.utils.ImmutableMap;
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
import java.util.Map;

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
    SageMakerClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
        final DescribeInferenceExperimentResponse describeInferenceExperimentResponse =
                getSdkDescribeResponse("Created", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final CreateInferenceExperimentResponse createInferenceExperimentResponse = CreateInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeInferenceExperimentResponse);
        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenReturn(createInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final Map<String, String> stackTags = ImmutableMap.of(
                "StackName", "TestStack",
                "StackId", "TestStackId");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .desiredResourceTags(stackTags)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResponseResourceModel(describeInferenceExperimentResponse.statusAsString(), null);

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

        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testCreateHandler_ResourceInUseException() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsServiceException validationFailureException = SageMakerException.builder()
                .message("1 validation error detected: Value null at 'Name' " +
                            "failed to satisfy constraint: Member must not be null")
                .statusCode(400)
                .build();

        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "Failure reason: 1 validation error detected: Value null at 'Name' failed to satisfy constraint: Member must not be null"));
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: null"));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_Successful() {
        final DescribeInferenceExperimentResponse firstDescribeResponse =
                getSdkDescribeResponse("Creating", null);

        final DescribeInferenceExperimentResponse secondDescribeResponse =
                getSdkDescribeResponse("Running", null);

        final CreateInferenceExperimentResponse createInferenceExperimentResponse = CreateInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenReturn(createInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResponseResourceModel(secondDescribeResponse.statusAsString(), null);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_VerifyStabilization_Failed() {
        final DescribeInferenceExperimentResponse firstDescribeResponse =
                getSdkDescribeResponse("Creating", null);

        final DescribeInferenceExperimentResponse secondDescribeResponse =
                getSdkDescribeResponse("Cancelled", TEST_ERROR_MESSAGE);

        final CreateInferenceExperimentResponse createInferenceExperimentResponse = CreateInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().createInferenceExperiment(any(CreateInferenceExperimentRequest.class)))
                .thenReturn(createInferenceExperimentResponse);
        
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing of {\"/properties/Name\":\"testInferenceExperimentName\"}"));
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .endpointName(TEST_ENDPOINT_NAME)
                .endpointMetadata(getCfnEndpointMetadata())
                .schedule(getCfnSchedule())
                .dataStorageConfig(getCfnDataStorageConfig())
                .modelVariants(getCfnModelVariants())
                .shadowModeConfig(getCfnShadowModeConfig())
                .build();
    }

    private ResourceModel getResponseResourceModel(final String status, final String statusReason) {
        return ResourceModel.builder()
                .arn(TEST_EXPERIMENT_ARN)
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .endpointName(TEST_ENDPOINT_NAME)
                .endpointMetadata(getCfnEndpointMetadata())
                .schedule(getCfnSchedule())
                .dataStorageConfig(getCfnDataStorageConfig())
                .modelVariants(getCfnModelVariants())
                .shadowModeConfig(getCfnShadowModeConfig())
                .endpointMetadata(getCfnEndpointMetadata())
                .creationTime(DateUtils.formatIso8601Date(TEST_TIME))
                .lastModifiedTime(DateUtils.formatIso8601Date(TEST_TIME))
                .status(status)
                .statusReason(statusReason)
                .desiredState(status)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}