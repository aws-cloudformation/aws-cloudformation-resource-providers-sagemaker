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
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

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
    public void testReadHandler_SimpleSuccess() {
        final DescribeProjectResponse describeProjectResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.CREATE_COMPLETED)
                        .build();
        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(describeProjectResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectArn(TEST_PROJECT_ARN)
                .projectName(TEST_PROJECT_NAME)
                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .tags(TEST_CFN_MODEL_TAGS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedResourceModel, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
        verify(proxyClient.client()).describeProject(any(DescribeProjectRequest.class));
    }

    @Test
    public void testReadHandler_WithEmptyTags() {
        final DescribeProjectResponse describeProjectResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
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

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectArn(TEST_PROJECT_ARN)
                .projectName(TEST_PROJECT_NAME)
                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedResourceModel, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
        verify(proxyClient.client()).describeProject(any(DescribeProjectRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ), exception.getMessage());
    }

    @Test
    public void testReadHandler_ProjectNotExist_Fails() {
        final ResourceNotFoundException resourceNotExistexception = ResourceNotFoundException.builder()
                .message(PROJECT_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenThrow(resourceNotExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }


    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.project.ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .projectName(TEST_PROJECT_NAME)
                .build();
    }
}
