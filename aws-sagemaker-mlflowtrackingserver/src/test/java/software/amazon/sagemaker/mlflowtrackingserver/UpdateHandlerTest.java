package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.*;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
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
import software.amazon.cloudformation.proxy.delay.Constant;

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
    private SageMakerClient sageMakerClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sageMakerClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sageMakerClient);
    }

    @Test
    public void testUpdateHandler() {
        assertThat(new UpdateHandler()).isNotNull();
        assertThat(new UpdateHandler(TEST_BACKOFF_STRATEGY)).isNotNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_NoTags() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse updatedDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATED, false);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse)
                .thenReturn(updatedDescribeResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(false, false, false))
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(false, false, false);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_AddTags() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        final Map<String, String> desiredResourceTags = TEST_RESOURCE_TAGS.stream().collect(Collectors.toMap(
                Tag::key, Tag::value, (oldValue, newValue) -> newValue));
        final List<Tag> allDesiredTags = new ArrayList<>(TEST_SDK_TAGS);
        allDesiredTags.addAll(TEST_RESOURCE_TAGS);

        final ListTagsResponse listTagsResponseWithTags = ListTagsResponse.builder()
                .tags(allDesiredTags)
                .build();
        final AddTagsResponse addTagsResponse = AddTagsResponse.builder()
                .tags(allDesiredTags)
                .build();

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithTags); // addTagsToModel in ReadHandler
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenReturn(addTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(false, false, false))
                .desiredResourceState(createResourceModel(true, false, false))
                .desiredResourceTags(desiredResourceTags)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(true, true, false);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_AddTagsFailure_AccessDeniedException() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        final Map<String, String> desiredResourceTags = TEST_RESOURCE_TAGS.stream().collect(Collectors.toMap(
                Tag::key, Tag::value, (oldValue, newValue) -> newValue));

        final String errorCode = "AccessDeniedException";
        final AwsServiceException accessDeniedException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage("test error message")
                        .build())
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse);
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenThrow(accessDeniedException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(false, false, false))
                .desiredResourceState(createResourceModel(true, false, false))
                .desiredResourceTags(desiredResourceTags)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AccessDenied);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.AccessDenied.getMessage(),
                Action.UPDATE, errorCode));
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_RemoveTags() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse updatedDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATED, false);

        final Map<String, String> previousResourceTags = TEST_RESOURCE_TAGS.stream().collect(Collectors.toMap(
                Tag::key, Tag::value, (oldValue, newValue) -> newValue));

        final ListTagsResponse listTagsResponseWithoutTags = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();
        final DeleteTagsResponse deleteTagsResponse = DeleteTagsResponse.builder()
                .build();

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse)
                .thenReturn(updatedDescribeResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithoutTags); // addTagsToModel in ReadHandler
        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenReturn(deleteTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false))
                .previousResourceTags(previousResourceTags)
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(false, false, false);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_AtDescribe_ResourceNotFound() {
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(true, false, true))
                .build();
        final Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME));
    }

    @Test
    public void testUpdateHandler_AtDescribe_ServiceInternalException() {
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
                .previousResourceState(createResourceModel(true, false, false))
                .desiredResourceState(createResourceModel(true, false, true))
                .build();
        final Exception exception = assertThrows(CfnServiceInternalErrorException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.ServiceInternalError.getMessage(), Action.READ));
    }

    @Test
    public void testUpdateHandler_AtUpdate_ResourceNotFound() {
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
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenThrow(resourceNotFound);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false))
                .desiredResourceState(createResourceModel(true, false, true))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_TRACKING_SERVER_NAME));
    }

    @Test
    public void testUpdateHandler_AtUpdate_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("InternalError")
                        .errorMessage("test error message")
                        .build())
                .message("test error message")
                .statusCode(500)
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenThrow(serviceInternalException);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false))
                .desiredResourceState(createResourceModel(true, false, true))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
        assertThat(response.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                Action.UPDATE));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_DowngradeMlflowMajorVersion() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .desiredResourceState(createResourceModel(true, false, false, TEST_MAJOR_DOWNGRADED_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(
                TrackingServerStatus.CREATED, false, TEST_MLFLOW_VERSION);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Resource update request is invalid"));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_DowngradeMlflowMinorVersion() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .desiredResourceState(createResourceModel(true, false, false, TEST_MINOR_DOWNGRADED_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(
                TrackingServerStatus.CREATED, false, TEST_MLFLOW_VERSION);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Resource update request is invalid"));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_DowngradeMlflowPatchVersion() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .desiredResourceState(createResourceModel(true, false, false, TEST_PATCH_DOWNGRADED_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(
                TrackingServerStatus.CREATED, false, TEST_MLFLOW_VERSION);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Resource update request is invalid"));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_UpgradeMlflowMajorVersion() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .desiredResourceState(createResourceModel(true, false, false, TEST_MAJOR_UPGRADED_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(
                TrackingServerStatus.CREATED, false, TEST_MLFLOW_VERSION);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Resource update request is invalid"));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_UpgradeMlflowMinorVersion() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .desiredResourceState(createResourceModel(true, false, false, TEST_MINOR_UPGRADED_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(
                TrackingServerStatus.CREATED, false, TEST_MLFLOW_VERSION);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Resource update request is invalid"));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_UpgradeMlflowPatchVersion() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .desiredResourceState(createResourceModel(true, false, false, TEST_PATCH_UPGRADED_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(
                TrackingServerStatus.CREATED, false, TEST_MLFLOW_VERSION);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Resource update request is invalid"));
    }

    @Test
    public void testUpdateHandler_InvalidRequestException_ChangeRoleArn() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN))
                .desiredResourceState(createResourceModel(true, false, false, TEST_MLFLOW_VERSION, TEST_UPDATED_ROLE_ARN))
                .build();
        final DescribeMlflowTrackingServerResponse initialDescribeResponse = createDescribeMlflowTrackingServerResponse(
                TrackingServerStatus.CREATED, false, TEST_MLFLOW_VERSION);

        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(initialDescribeResponse);

        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Resource update request is invalid"));
    }

    @Test
    public void testUpdateHandler_StabilizationFailed_UnknownStatus() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse unknownDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UNKNOWN_TO_SDK_VERSION, false);

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse) // getting existing state
                .thenReturn(unknownDescribeResponse); // second stabilize call

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false))
                .desiredResourceState(createResourceModel(true, false, true))
                .build();

        final Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                String.format("Stabilizing of %s failed with an unexpected status %s",
                        TEST_TRACKING_SERVER_ARN, TrackingServerStatus.UNKNOWN_TO_SDK_VERSION)));
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_NoChange_OnlyStackTagUpdate() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);

        final Map<String, String> desiredResourceTags = TEST_RESOURCE_TAGS.stream().collect(Collectors.toMap(
                Tag::key, Tag::value, (oldValue, newValue) -> newValue));
        final List<Tag> allDesiredTags = new ArrayList<>(TEST_SDK_TAGS);
        allDesiredTags.addAll(TEST_RESOURCE_TAGS);
        final ListTagsResponse listTagsResponseWithTags = ListTagsResponse.builder()
                .tags(allDesiredTags)
                .build();
        final AddTagsResponse addTagsResponse = AddTagsResponse.builder()
                .tags(allDesiredTags)
                .build();

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithTags); // addTagsToModel in ReadHandler
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenReturn(addTagsResponse);

        // Supply only the bare minimum properties during an update request
        final ResourceModel previousModel = createResourceModel(true, false, false);
        final ResourceModel desiredModel = createResourceModel(true, false, false);
        desiredModel.setMlflowVersion(null);
        desiredModel.setArtifactStoreUri(null);
        desiredModel.setAutomaticModelRegistration(null);
        desiredModel.setTrackingServerSize(null);
        desiredModel.setWeeklyMaintenanceWindowStart(null);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(previousModel)
                .desiredResourceState(desiredModel)
                .desiredResourceTags(desiredResourceTags)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(true, true, false);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_UpdateProperties() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse updatingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATING, false);
        final DescribeMlflowTrackingServerResponse updatedDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATED, true);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse) // getting existing state
                .thenReturn(updatingDescribeResponse) // first stabilize call
                .thenReturn(updatedDescribeResponse); // second stabilize call
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false))
                .desiredResourceState(createResourceModel(true, false, true))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(true, false, true);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_UpdateFailedStatus() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse updatingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATING, false);
        final DescribeMlflowTrackingServerResponse updateFailedDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATE_FAILED, false);

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse) // getting existing state
                .thenReturn(updatingDescribeResponse) // first stabilize call
                .thenReturn(updateFailedDescribeResponse); // second stabilize call

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false))
                .desiredResourceState(createResourceModel(true, false, true))
                .build();
        assertThrows(CfnNotStabilizedException.class, () -> invokeHandler(request));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_ThrottlingErrors() {
        final UpdateMlflowTrackingServerResponse updateMlflowTrackingServerResponse = UpdateMlflowTrackingServerResponse.builder()
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .build();
        final DescribeMlflowTrackingServerResponse createdDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.CREATED, false);
        final DescribeMlflowTrackingServerResponse updatingDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATING, false);
        final DescribeMlflowTrackingServerResponse updatedDescribeResponse = createDescribeMlflowTrackingServerResponse(TrackingServerStatus.UPDATED, true);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        final Exception rateLimitExceeded = SageMakerException.builder()
                .message("Rate limit exceeded")
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ThrottlingException")
                        .errorMessage("Rate limit exceeded")
                        .build())
                .build();

        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenThrow(rateLimitExceeded);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse); // second stabilize call

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(true, false, false))
                .desiredResourceState(createResourceModel(true, false, true))
                .build();

        // Create a single shared UpdateHandler
        final UpdateHandler handler = new UpdateHandler(TEST_BACKOFF_STRATEGY);
        // Create a single shared callbackContext and use it to call the UpdateHandler's handleRequest repeatedly.
        final CallbackContext callbackContext = new CallbackContext();

        final ProgressEvent<ResourceModel, CallbackContext> firstAttemptResponse =
                invokeHandler(request, handler, callbackContext);

        assertThat(firstAttemptResponse).isNotNull();
        assertThat(firstAttemptResponse.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(firstAttemptResponse.getErrorCode()).isEqualTo(HandlerErrorCode.Throttling);
        assertThat(firstAttemptResponse.getMessage()).isNull();

        // Make the second attempt without throttling
        when(proxyClient.client().updateMlflowTrackingServer(any(UpdateMlflowTrackingServerRequest.class)))
                .thenReturn(updateMlflowTrackingServerResponse);
        when(proxyClient.client().describeMlflowTrackingServer(any(DescribeMlflowTrackingServerRequest.class)))
                .thenReturn(createdDescribeResponse) // getting existing state
                .thenReturn(updatingDescribeResponse) // first stabilize call
                .thenReturn(updatedDescribeResponse); // second stabilize call
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> secondAttemptResponse =
                invokeHandler(request, handler, firstAttemptResponse.getCallbackContext());

        final ResourceModel expectedModelFromResponse = createResourceModel(true, false, true);

        assertThat(secondAttemptResponse).isNotNull();
        assertThat(secondAttemptResponse.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(secondAttemptResponse.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(secondAttemptResponse.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(secondAttemptResponse.getResourceModels()).isNull();
        assertThat(secondAttemptResponse.getMessage()).isNull();
        assertThat(secondAttemptResponse.getErrorCode()).isNull();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        return invokeHandler(request, new UpdateHandler(TEST_BACKOFF_STRATEGY), new CallbackContext());
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(
            final ResourceHandlerRequest<ResourceModel> request, final UpdateHandler handler, final CallbackContext callbackContext) {
        return handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);
    }
}
