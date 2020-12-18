package software.amazon.sagemaker.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ProjectStatus;
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
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    private final ResourceModel requestModel = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .projectName(TEST_PROJECT_NAME)
            .projectArn(TEST_PROJECT_ARN)
            .serviceCatalogProvisioningDetails(
                    ServiceCatalogProvisioningDetails.builder()
                    .productId(TEST_PRODUCT_ID)
                    .pathId(TEST_PATH_ID)
                    .provisioningArtifactId(TEST_PROVISIONING_ARTIFACT_ID)
                    .provisioningParameters(Arrays.asList(
                            ProvisioningParameter.builder()
                                    .key("key1")
                                    .value("value1")
                            .build()))
                    .build())
            .build();

    private final ResourceModel requestModelWithTags = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .projectName(TEST_PROJECT_NAME)
            .projectArn(TEST_PROJECT_ARN)
            .serviceCatalogProvisioningDetails(
                    ServiceCatalogProvisioningDetails.builder()
                            .productId(TEST_PRODUCT_ID)
                            .pathId(TEST_PATH_ID)
                            .provisioningArtifactId(TEST_PROVISIONING_ARTIFACT_ID)
                            .build())
            .tags(TEST_CFN_MODEL_TAGS)
            .build();


    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
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

        final CreateProjectResponse createProjectResponse = CreateProjectResponse.builder()
                .projectArn(TEST_PROJECT_ARN)
                .build();
        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenReturn(createProjectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectName(TEST_PROJECT_NAME)
                .projectArn(TEST_PROJECT_ARN)
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
    public void testCreateHandler_withTags() {
        final DescribeProjectResponse describeProjectResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectName(TEST_PROJECT_NAME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectStatus(ProjectStatus.CREATE_COMPLETED)
                        .build();
        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(describeProjectResponse);

        final CreateProjectResponse createProjectResponse = CreateProjectResponse.builder()
                .projectArn(TEST_PROJECT_ARN)
                .build();
        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenReturn(createProjectResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModelWithTags)
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectName(TEST_PROJECT_NAME)
                .projectArn(TEST_PROJECT_ARN)
                .projectStatus(ProjectStatus.CREATE_COMPLETED.toString())
                .tags(TEST_CFN_MODEL_TAGS)
                .build();

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedModelFromResponse, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ProjectAlreadyExists_Fails() {
        final ResourceInUseException resourceExistexception = ResourceInUseException.builder()
                .message(PROJECT_ALREADY_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenThrow(resourceExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ResourceInUseException_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PROJECT_NAME), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsServiceException validationFailureException = SageMakerException.builder()
                .message("1 validation error detected: Value null at 'ProjectName' " +
                        "failed to satisfy constraint: Member must not be null")
                .statusCode(400)
                .build();

        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                Action.CREATE), exception.getMessage());
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE), exception.getMessage());
    }

    @Test
    public void testCreateHandler_VerifyStabilization_CompletedStatus() {
        final DescribeProjectResponse firstDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.PENDING)
                        .build();

        final DescribeProjectResponse secondDescribeResponse =
                DescribeProjectResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .projectArn(TEST_PROJECT_ARN)
                        .projectName(TEST_PROJECT_NAME)
                        .projectStatus(ProjectStatus.CREATE_COMPLETED)
                        .build();
        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);

        final CreateProjectResponse createProjectResponse = CreateProjectResponse.builder()
                .projectArn(TEST_PROJECT_ARN)
                .build();

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse).thenReturn(listTagsResponse);

        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenReturn(createProjectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

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
    public void testCreateHandler_VerifyStabilization_FailedStatus() {
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

        final CreateProjectResponse createProjectResponse = CreateProjectResponse.builder()
                .projectArn(TEST_PROJECT_ARN)
                .build();

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        when(proxyClient.client().describeProject(any(DescribeProjectRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createProject(any(CreateProjectRequest.class)))
                .thenReturn(createProjectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .projectArn(TEST_PROJECT_ARN)
                .projectName(TEST_PROJECT_NAME)
                .projectStatus("DeleteFailed")
                .build();

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedModelFromResponse, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.project.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.project.CreateHandler handler = new software.amazon.sagemaker.project.CreateHandler();
        return handler.handleRequest(proxy, request, new software.amazon.sagemaker.project.CallbackContext(), proxyClient, logger);
    }
}
