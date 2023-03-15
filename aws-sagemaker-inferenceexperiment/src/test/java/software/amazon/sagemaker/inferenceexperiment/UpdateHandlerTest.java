package software.amazon.sagemaker.inferenceexperiment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.StartInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.StartInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateInferenceExperimentResponse;
import software.amazon.awssdk.utils.DateUtils;
import software.amazon.awssdk.utils.ImmutableMap;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

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
    public void testUpdateHandler_UpdateResource_Success() {
        final DescribeInferenceExperimentResponse describeInferenceExperimentResponse =
                getSdkDescribeResponse("Running", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final UpdateInferenceExperimentResponse updateInferenceExperimentResponse = UpdateInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeInferenceExperimentResponse);
        when(proxyClient.client().updateInferenceExperiment(any(UpdateInferenceExperimentRequest.class)))
                .thenReturn(updateInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
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
    public void testUpdateHandler_UpdateTags_Success() {
        final DescribeInferenceExperimentResponse describeInferenceExperimentResponse =
                getSdkDescribeResponse("Running", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(Arrays.asList(
                        Tag.builder()
                                .key("existing-key")
                                .value("existing-value")
                                .build(),
                        Tag.builder()
                                .key("remove-key")
                                .value("remove-value")
                                .build())
                )
                .build();
        final ListTagsResponse updatedListTagsResponse = ListTagsResponse.builder()
                .tags(Arrays.asList(
                        Tag.builder()
                                .key("existing-key")
                                .value("new-value")
                                .build(),
                        Tag.builder()
                                .key("new-key")
                                .value("new-value")
                                .build())
                )
                .build();

        final UpdateInferenceExperimentResponse updateInferenceExperimentResponse = UpdateInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeInferenceExperimentResponse);
        when(proxyClient.client().updateInferenceExperiment(any(UpdateInferenceExperimentRequest.class)))
                .thenReturn(updateInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse)
                .thenReturn(updatedListTagsResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setTags(Arrays.asList(
                software.amazon.sagemaker.inferenceexperiment.Tag.builder()
                        .key("existing-key")
                        .value("new-value")
                        .build(),
                software.amazon.sagemaker.inferenceexperiment.Tag.builder()
                        .key("new-key")
                        .value("new-value")
                        .build()
        ));

        final Map<String, String> stackTags = ImmutableMap.of(
                "StackName", "TestStack",
                "StackId", "TestStackId");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .desiredResourceTags(stackTags)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResponseResourceModel(describeInferenceExperimentResponse.statusAsString(), null);
        expectedModelFromResponse.setTags(requestResourceModel.getTags());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_StartResource_Success() {
        final DescribeInferenceExperimentResponse firstDescribeResponse =
                getSdkDescribeResponse("Created", null);
        final DescribeInferenceExperimentResponse secondDescribeResponse =
                getSdkDescribeResponse("Running", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final StartInferenceExperimentResponse startInferenceExperimentResponse = StartInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().startInferenceExperiment(any(StartInferenceExperimentRequest.class)))
                .thenReturn(startInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Running");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
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
    public void testUpdateHandler_StopResource_Success() {
        final DescribeInferenceExperimentResponse firstDescribeResponse =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse secondDescribeResponse =
                getSdkDescribeResponse("Completed", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
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
    public void testUpdateHandler_StopResource_PromoteShadow_Success() {
        final DescribeInferenceExperimentResponse firstDescribeResponse =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse secondDescribeResponse =
                getSdkDescribeResponse("Completed", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");
        requestResourceModel.setModelVariants(Collections.singletonList(getCfnModelVariantConfig(TEST_SHADOW_MODEL_NAME, TEST_SHADOW_VARIANT_NAME)));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
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
    public void testUpdateHandler_StopResource_RemoveShadow_Success() {
        final DescribeInferenceExperimentResponse firstDescribeResponse =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse secondDescribeResponse =
                getSdkDescribeResponse("Completed", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");
        requestResourceModel.setModelVariants(Collections.singletonList(getCfnModelVariantConfig(TEST_PROD_MODEL_NAME, TEST_PROD_VARIANT_NAME)));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
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
    public void testUpdateHandler_UpdateResource_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Running", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().updateInferenceExperiment(any(UpdateInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testUpdateHandler_UpdateResource_ResourceNotFoundException() {
        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Running", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().updateInferenceExperiment(any(UpdateInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    @Test
    public void testUpdateHandler_StartResource_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Created", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().startInferenceExperiment(any(StartInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Running");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testUpdateHandler_StartResource_ResourceNotFoundException() {
        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Created", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().startInferenceExperiment(any(StartInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Running");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    @Test
    public void testUpdateHandler_StopResource_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Running", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Cancelled");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testUpdateHandler_StopResource_ResourceNotFoundException() {
        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Running", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Cancelled");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    @Test
    public void testUpdateHandler_ReadResource_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testUpdateHandler_ReadResource_ResourceNotFoundException() {
        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    @Test
    public void testUpdateHandler_ResourceLimitExceededException() {
        final DescribeInferenceExperimentResponse describeResponse =
                getSdkDescribeResponse("Running", null);

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().updateInferenceExperiment(any(UpdateInferenceExperimentRequest.class)))
                .thenThrow(ResourceLimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: null"));
    }

    @Test
    public void testUpdateHandler_UpdateResource_VerifyStabilization_Successful() {
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse describeResponseWithUpdatingStatus =
                getSdkDescribeResponse("Updating", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final UpdateInferenceExperimentResponse updateInferenceExperimentResponse = UpdateInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithUpdatingStatus)
                .thenReturn(describeResponseWithRunningStatus);
        when(proxyClient.client().updateInferenceExperiment(any(UpdateInferenceExperimentRequest.class)))
                .thenReturn(updateInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResponseResourceModel(describeResponseWithRunningStatus.statusAsString(), null);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void  testUpdateHandler_UpdateResource_VerifyStabilization_Failed() {
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse describeResponseWithCancelledStatus =
                getSdkDescribeResponse("Cancelled", null);
        final DescribeInferenceExperimentResponse describeResponseWithUpdatingStatus =
                getSdkDescribeResponse("Updating", null);

        final UpdateInferenceExperimentResponse updateInferenceExperimentResponse = UpdateInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithUpdatingStatus)
                .thenReturn(describeResponseWithCancelledStatus);
        when(proxyClient.client().updateInferenceExperiment(any(UpdateInferenceExperimentRequest.class)))
                .thenReturn(updateInferenceExperimentResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during update of {\"/properties/Name\":\"testInferenceExperimentName\"}"));
    }

    @Test
    public void testUpdateHandler_StartResource_VerifyStabilization_Successful() {
        final DescribeInferenceExperimentResponse describeResponseWithCreatedStatus =
                getSdkDescribeResponse("Created", null);
        final DescribeInferenceExperimentResponse describeResponseWithStartingStatus =
                getSdkDescribeResponse("Starting", null);
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final StartInferenceExperimentResponse startInferenceExperimentResponse = StartInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithCreatedStatus)
                .thenReturn(describeResponseWithStartingStatus)
                .thenReturn(describeResponseWithRunningStatus);
        when(proxyClient.client().startInferenceExperiment(any(StartInferenceExperimentRequest.class)))
                .thenReturn(startInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Running");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResponseResourceModel(describeResponseWithRunningStatus.statusAsString(), null);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void  testUpdateHandler_StartResource_VerifyStabilization_Failed() {
        final DescribeInferenceExperimentResponse describeResponseWithCreatedStatus =
                getSdkDescribeResponse("Created", null);
        final DescribeInferenceExperimentResponse describeResponseWithStartingStatus =
                getSdkDescribeResponse("Starting", null);
        final DescribeInferenceExperimentResponse describeResponseWithCancelledStatus =
                getSdkDescribeResponse("Cancelled", null);

        final StartInferenceExperimentResponse startInferenceExperimentResponse = StartInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithCreatedStatus)
                .thenReturn(describeResponseWithStartingStatus)
                .thenReturn(describeResponseWithCancelledStatus);
        when(proxyClient.client().startInferenceExperiment(any(StartInferenceExperimentRequest.class)))
                .thenReturn(startInferenceExperimentResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Running");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during start of {\"/properties/Name\":\"testInferenceExperimentName\"}"));
    }

    @Test
    public void testUpdateHandler_StopResource_VerifyStabilization_Successful() {
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse describeResponseWithStoppingStatus =
                getSdkDescribeResponse("Stopping", null);
        final DescribeInferenceExperimentResponse describeResponseWithCompletedStatus =
                getSdkDescribeResponse("Completed", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder().build();

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithStoppingStatus)
                .thenReturn(describeResponseWithCompletedStatus);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = getResponseResourceModel(describeResponseWithCompletedStatus.statusAsString(), null);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void  testUpdateHandler_StopResource_VerifyStabilization_Failed() {
        final DescribeInferenceExperimentResponse describeResponseWithRunningStatus =
                getSdkDescribeResponse("Running", null);
        final DescribeInferenceExperimentResponse describeResponseWithStoppingStatus =
                getSdkDescribeResponse("Stopping", null);

        final StopInferenceExperimentResponse stopInferenceExperimentResponse = StopInferenceExperimentResponse.builder()
                .inferenceExperimentArn(TEST_EXPERIMENT_ARN)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithRunningStatus)
                .thenReturn(describeResponseWithStoppingStatus)
                .thenReturn(describeResponseWithRunningStatus);
        when(proxyClient.client().stopInferenceExperiment(any(StopInferenceExperimentRequest.class)))
                .thenReturn(stopInferenceExperimentResponse);

        final ResourceModel requestResourceModel = getRequestResourceModel();
        requestResourceModel.setDesiredState("Completed");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during stop of {\"/properties/Name\":\"testInferenceExperimentName\"}"));
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
        final UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}