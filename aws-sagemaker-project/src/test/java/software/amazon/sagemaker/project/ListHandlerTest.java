package software.amazon.sagemaker.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListProjectsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListProjectsResponse;
import software.amazon.awssdk.services.sagemaker.model.ProjectStatus;
import software.amazon.awssdk.services.sagemaker.model.ProjectSummary;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

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
        final ProjectSummary projectSummary = ProjectSummary.builder()
                .creationTime(TEST_CREATION_TIME)
                .projectArn(TEST_PROJECT_ARN)
                .projectName(TEST_PROJECT_NAME)
                .projectStatus(ProjectStatus.CREATE_COMPLETED)
                .build();

        final ListProjectsResponse listProjectsResponse =
                ListProjectsResponse.builder()
                        .projectSummaryList(projectSummary)
                        .nextToken(TEST_TOKEN)
                        .build();

        when(proxyClient.client().listProjects(any(ListProjectsRequest.class)))
                .thenReturn(listProjectsResponse);

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectArn(TEST_PROJECT_ARN)
                .projectName(TEST_PROJECT_NAME)
//                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .build();

        List<ResourceModel> expectedModels = new ArrayList<ResourceModel>();
        expectedModels.add(expectedResourceModel);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getResourceModel());
        assertEquals(expectedModels, response.getResourceModels());
        assertEquals(TEST_TOKEN, response.getNextToken());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testListHandler_SimpleSuccess_NoProjectExist() {
        final ListProjectsResponse listProjectsResponse =
                ListProjectsResponse.builder()
                        .projectSummaryList(Collections.emptyList())
                        .nextToken(null)
                        .build();

        when(proxyClient.client().listProjects(any(ListProjectsRequest.class)))
                .thenReturn(listProjectsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getResourceModel());
        assertEquals(Collections.emptyList(), response.getResourceModels());
        assertNull(response.getNextToken());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testListHandler_ServiceInternalException() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("InternalError").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        when(proxyClient.client().listProjects(any(ListProjectsRequest.class)))
                .thenThrow(ex);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnServiceInternalErrorException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                Action.LIST.toString()), exception.getMessage());
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.project.ListHandler handler = new ListHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder().build();
    }
}
