package software.amazon.sagemaker.modelpackagegroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.GetModelPackageGroupPolicyResponse;
import software.amazon.awssdk.services.sagemaker.model.GetModelPackageGroupPolicyRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SageMakerClient> proxyClient;

    @Mock
    SageMakerClient sdkClient;

    private final ResourceModel requestModel = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
            .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
            .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
            .build();

    private final ResourceModel requestModelWithTags = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
            .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
            .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
            .tags(TEST_CFN_MODEL_TAGS)
            .build();

    private final ResourceModel requestModelWithResourcePolicy = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
            .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
            .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
            .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY)
            .build();

    private final ResourceModel requestModelWithTagsAndResourcePolicy = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
            .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
            .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
            .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY)
            .build();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
        final DescribeModelPackageGroupResponse describeModelPackageGroupResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(describeModelPackageGroupResponse);

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse =
                GetModelPackageGroupPolicyResponse.builder()
                        .resourcePolicy(null)
                        .build();
        when(proxyClient.client().getModelPackageGroupPolicy(any(GetModelPackageGroupPolicyRequest.class)))
                .thenReturn(getModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final CreateModelPackageGroupResponse createModelPackageGroupResponse = CreateModelPackageGroupResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();
        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenReturn(createModelPackageGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
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
        final DescribeModelPackageGroupResponse describeModelPackageGroupResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(describeModelPackageGroupResponse);

        final CreateModelPackageGroupResponse createModelPackageGroupResponse = CreateModelPackageGroupResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();
        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenReturn(createModelPackageGroupResponse);

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse =
                GetModelPackageGroupPolicyResponse.builder()
                        .resourcePolicy(null)
                        .build();
        when(proxyClient.client().getModelPackageGroupPolicy(any(GetModelPackageGroupPolicyRequest.class)))
                .thenReturn(getModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModelWithTags)
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
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
    public void testCreateHandler_withResourcePolicy() {
        final DescribeModelPackageGroupResponse describeModelPackageGroupResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(describeModelPackageGroupResponse);

        final CreateModelPackageGroupResponse createModelPackageGroupResponse = CreateModelPackageGroupResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();
        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenReturn(createModelPackageGroupResponse);

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse =
                GetModelPackageGroupPolicyResponse.builder()
                        .resourcePolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
                        .build();
        when(proxyClient.client().getModelPackageGroupPolicy(any(GetModelPackageGroupPolicyRequest.class)))
                .thenReturn(getModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModelWithResourcePolicy)
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
                .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY)
                .build();

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedModelFromResponse, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testCreateHandler_withTagsAndResourcePolicy() {
        final DescribeModelPackageGroupResponse describeModelPackageGroupResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(describeModelPackageGroupResponse);

        final CreateModelPackageGroupResponse createModelPackageGroupResponse = CreateModelPackageGroupResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();
        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenReturn(createModelPackageGroupResponse);

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse =
                GetModelPackageGroupPolicyResponse.builder()
                        .resourcePolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
                        .build();
        when(proxyClient.client().getModelPackageGroupPolicy(any(GetModelPackageGroupPolicyRequest.class)))
                .thenReturn(getModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModelWithResourcePolicy)
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
                .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY)
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

        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ModelPackageGroupAlreadyExists_Fails() {
        final AwsServiceException resourceExistexception = SageMakerException.builder()
                .message(MODEL_PACKAGE_GROUP_ALREADY_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenThrow(resourceExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ResourceInUseException_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
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
                .message("1 validation error detected: Value null at 'modelPackageGroupName' " +
                        "failed to satisfy constraint: Member must not be null")
                .statusCode(400)
                .build();

        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
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

        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
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
        final DescribeModelPackageGroupResponse firstDescribeResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.PENDING)
                        .build();

        final DescribeModelPackageGroupResponse secondDescribeResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);

        final CreateModelPackageGroupResponse createModelPackageGroupResponse = CreateModelPackageGroupResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse =
                GetModelPackageGroupPolicyResponse.builder()
                        .resourcePolicy(null)
                        .build();
        when(proxyClient.client().getModelPackageGroupPolicy(any(GetModelPackageGroupPolicyRequest.class)))
                .thenReturn(getModelPackageGroupPolicyResponse).thenReturn(getModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse).thenReturn(listTagsResponse);

        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenReturn(createModelPackageGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
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
        final DescribeModelPackageGroupResponse firstDescribeResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.PENDING)
                        .build();

        final DescribeModelPackageGroupResponse secondDescribeResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.DELETE_FAILED)
                        .build();

        final CreateModelPackageGroupResponse createModelPackageGroupResponse = CreateModelPackageGroupResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createModelPackageGroup(any(CreateModelPackageGroupRequest.class)))
                .thenReturn(createModelPackageGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing of {\"/properties/ModelPackageGroupArn\":\"testModelPackageGroupArn\"} failed with unexpected status DeleteFailed"),
                exception.getMessage());
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.modelpackagegroup.CreateHandler handler = new software.amazon.sagemaker.modelpackagegroup.CreateHandler();
        return handler.handleRequest(proxy, request, new software.amazon.sagemaker.modelpackagegroup.CallbackContext(), proxyClient, logger);
    }
}
