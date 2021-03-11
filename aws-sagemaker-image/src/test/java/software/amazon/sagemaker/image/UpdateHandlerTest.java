package software.amazon.sagemaker.image;

import java.time.Duration;
import java.util.Collections;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.AddTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.UpdateImageRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateImageResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
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
    public void testUpdateHandler_SimpleSuccess_NoTags() {
        final UpdateImageResponse updateImageResponse = UpdateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenReturn(updateImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), false))
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(ImageStatus.CREATED.toString(), false);

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
        final UpdateImageResponse updateImageResponse = UpdateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final ListTagsResponse listTagsResponseWithoutTags = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();
        final ListTagsResponse listTagsResponseWithTags = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();
        final AddTagsResponse addTagsResponse = AddTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenReturn(updateImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithoutTags).thenReturn(listTagsResponseWithTags);
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenReturn(addTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(ImageStatus.CREATED.toString(), true);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_RemoveTags() {
        final UpdateImageResponse updateImageResponse = UpdateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final ListTagsResponse listTagsResponseWithoutTags = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();
        final ListTagsResponse listTagsResponseWithTags = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();
        final DeleteTagsResponse deleteTagsResponse = DeleteTagsResponse.builder()
                .build();

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenReturn(updateImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithTags).thenReturn(listTagsResponseWithoutTags);
        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenReturn(deleteTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), false))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(ImageStatus.CREATED.toString(), false);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_RemoveOptionalField() {
        final UpdateImageResponse updateImageResponse = UpdateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse describeImageResponseWithDescription = DescribeImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .imageName(TEST_IMAGE_NAME)
                .roleArn(TEST_IMAGE_ROLE_ARN)
                .displayName(TEST_IMAGE_DISPLAY_NAME)
                .description(TEST_IMAGE_DESCRIPTION)
                .imageStatus(ImageStatus.CREATED)
                .creationTime(TEST_CREATION_TIME)
                .lastModifiedTime(TEST_LAST_MODIFIED_TIME)
                .build();
        final DescribeImageResponse describeImageResponseWithoutDescription = DescribeImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .imageName(TEST_IMAGE_NAME)
                .roleArn(TEST_IMAGE_ROLE_ARN)
                .displayName(TEST_IMAGE_DISPLAY_NAME)
                .imageStatus(ImageStatus.CREATED)
                .creationTime(TEST_CREATION_TIME)
                .lastModifiedTime(TEST_LAST_MODIFIED_TIME)
                .build();
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(Collections.emptyList())
                .build();

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenReturn(updateImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponseWithDescription).thenReturn(describeImageResponseWithoutDescription);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel desiredStateModel = ResourceModel.builder()
                .imageArn(TEST_IMAGE_ARN)
                .imageName(TEST_IMAGE_NAME)
                .imageRoleArn(TEST_IMAGE_ROLE_ARN)
                .imageDisplayName(TEST_IMAGE_DISPLAY_NAME)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredStateModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(desiredStateModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_ResourceNotFound() {
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
                .build();
        final Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_IMAGE_NAME));
    }

    @Test
    public void testUpdateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();
        final DescribeImageResponse describeImageResponse = createDescribeImageResponse(ImageStatus.CREATED);

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenThrow(serviceInternalException);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(describeImageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
                .build();
        final Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandler(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.GeneralServiceException.getMessage(), Action.UPDATE));
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_CreatedStatus() {
        final UpdateImageResponse updateImageResponse = UpdateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse updatingDescribeResponse = createDescribeImageResponse(ImageStatus.UPDATING);
        final DescribeImageResponse createdDescribeResponse = createDescribeImageResponse(ImageStatus.CREATED);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenReturn(updateImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(createdDescribeResponse) // getting existing state
                .thenReturn(updatingDescribeResponse) // first stabilize call
                .thenReturn(createdDescribeResponse); // second stabilize call
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandler(request);
        final ResourceModel expectedModelFromResponse = createResourceModel(ImageStatus.CREATED.toString(), true);

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
        final UpdateImageResponse updateImageResponse = UpdateImageResponse.builder()
                .imageArn(TEST_IMAGE_ARN)
                .build();
        final DescribeImageResponse updatingDescribeResponse = createDescribeImageResponse(ImageStatus.UPDATING);
        final DescribeImageResponse updateFailedDescribeResponse = createDescribeImageResponse(ImageStatus.UPDATE_FAILED);

        when(proxyClient.client().updateImage(any(UpdateImageRequest.class)))
                .thenReturn(updateImageResponse);
        when(proxyClient.client().describeImage(any(DescribeImageRequest.class)))
                .thenReturn(updateFailedDescribeResponse) // getting existing state
                .thenReturn(updatingDescribeResponse) // first stabilize call
                .thenReturn(updateFailedDescribeResponse); // second stabilize call

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(createResourceModel(ImageStatus.CREATED.toString(), true))
                .build();
        assertThrows(CfnNotStabilizedException.class, () -> invokeHandler(request));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandler(final ResourceHandlerRequest<ResourceModel> request) {
        final UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
