package software.amazon.sagemaker.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ProjectStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateHandlerTest extends AbstractTestBase {

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
        final DescribeProjectResponse describeProjectResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectName(TEST_PROJECT_NAME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectStatus(ProjectStatus.CREATE_COMPLETED)
                        .build();
        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(describeProjectResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithoutTags())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectArn(TEST_PROJECT_ARN)
                .projectName(TEST_PROJECT_NAME)
                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .build();

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedModelFromResponse, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_AddTags() {

        final ListTagsResponse listTagsResponseWithTags =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        final ListTagsResponse listTagsResponseWithoutTags =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithoutTags).thenReturn(listTagsResponseWithTags);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithTags())
                .build();

        assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_DeleteTags() {

        final ListTagsResponse listTagsResponseWithTags =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        final ListTagsResponse listTagsResponseWithoutTags =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponseWithTags).thenReturn(listTagsResponseWithoutTags);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithoutTags())
                .build();

        assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingTags() {

        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.project.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.project.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithTags())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.project.ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ProjectNotExists_UpdatingTags() {

        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
                .message(PROJECT_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.project.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.project.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithoutTags())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.project.ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }

    private ResourceModel getRequestResourceModelWithoutTags() {
        return ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectName(TEST_PROJECT_NAME)
                .projectArn(TEST_PROJECT_ARN)
                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .build();
    }

    private ResourceModel getRequestResourceModelWithTags() {
        return ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectName(TEST_PROJECT_NAME)
                .projectArn(TEST_PROJECT_ARN)
                .tags(TEST_CFN_MODEL_TAGS)
                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .build();
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectName(TEST_PROJECT_NAME)
                .projectArn(TEST_PROJECT_ARN)
                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .build();
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.project.UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
