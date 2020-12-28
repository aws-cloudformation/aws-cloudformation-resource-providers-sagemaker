package software.amazon.sagemaker.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

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
    public void testDeleteHandler_SimpleSuccess() {
        final DeleteProjectResponse deleteProjectResponse = DeleteProjectResponse.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenReturn(deleteProjectResponse);


        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
        assertNull(response.getResourceModel());
    }

    @Test
    public void testDeleteHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.DELETE), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_ProjectDoesNotExists_Fails() {
        final ResourceNotFoundException resourceNotExistexception = ResourceNotFoundException.builder()
                .message(PROJECT_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenThrow(resourceNotExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_ResourceNotFoundException_Fails() {
        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete_WithResourceNotFoundException() {
        final DescribeProjectResponse firstDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.DELETE_IN_PROGRESS)
                        .build();

        final DeleteProjectResponse deleteProjectResponse = DeleteProjectResponse.builder()
                .build();

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenThrow(ResourceNotFoundException.class)
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenReturn(deleteProjectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete_WithProjectNotExist() {
        final DescribeProjectResponse firstDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.DELETE_IN_PROGRESS)
                        .build();

        final AwsServiceException resourceNotExistexception = SageMakerException.builder()
                .message(PROJECT_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        final DeleteProjectResponse deleteProjectResponse = DeleteProjectResponse.builder()
                .build();

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenThrow(resourceNotExistexception)
                .thenThrow(resourceNotExistexception);
        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenReturn(deleteProjectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete_WithDeleteCompleted() {
        final DescribeProjectResponse firstDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.DELETE_IN_PROGRESS)
                        .build();

        final DescribeProjectResponse secondDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.DELETE_COMPLETED)
                        .build();

        final DeleteProjectResponse deleteProjectResponse = DeleteProjectResponse.builder()
                .build();

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenReturn(deleteProjectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_ResourceNotDeleted() {
        final DescribeProjectResponse firstDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.DELETE_IN_PROGRESS)
                        .build();

        final DescribeProjectResponse secondDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.DELETE_FAILED)
                        .build();

        final DeleteProjectResponse deleteProjectResponse = DeleteProjectResponse.builder()
                .build();

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(firstDescribeResponse)
                .thenReturn(secondDescribeResponse)
                .thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteProject(any(DeleteProjectRequest.class)))
                .thenReturn(deleteProjectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.FAILED, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .projectName(TEST_PROJECT_NAME)
                .build();
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.project.DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
