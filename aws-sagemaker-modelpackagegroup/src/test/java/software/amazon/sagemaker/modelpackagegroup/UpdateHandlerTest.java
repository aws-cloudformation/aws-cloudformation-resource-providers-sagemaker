package software.amazon.sagemaker.modelpackagegroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.PutModelPackageGroupPolicyResponse;
import software.amazon.awssdk.services.sagemaker.model.PutModelPackageGroupPolicyRequest;
import software.amazon.awssdk.services.sagemaker.model.GetModelPackageGroupPolicyResponse;
import software.amazon.awssdk.services.sagemaker.model.GetModelPackageGroupPolicyRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageGroupPolicyRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.AddTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
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
        final DescribeModelPackageGroupResponse describeModelPackageGroupResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(describeModelPackageGroupResponse);

        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse = GetModelPackageGroupPolicyResponse.builder()
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
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
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
    public void testUpdateHandler_SimpleSuccess_AddTags() {
        final DescribeModelPackageGroupResponse describeModelPackageGroupResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(describeModelPackageGroupResponse);

        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse = GetModelPackageGroupPolicyResponse.builder()
                .resourcePolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
                .build();

        when(proxyClient.client().getModelPackageGroupPolicy(any(GetModelPackageGroupPolicyRequest.class)))
                .thenReturn(getModelPackageGroupPolicyResponse);

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

        final AddTagsResponse addTagsResponse =
                AddTagsResponse.builder().build();
        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenReturn(addTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithBothTagsAndResourcePolicy())
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
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
    public void testUpdateHandler_SimpleSuccess_DeleteTags() {
        final DescribeModelPackageGroupResponse describeModelPackageGroupResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED)
                        .build();
        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(describeModelPackageGroupResponse);

        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final GetModelPackageGroupPolicyResponse getModelPackageGroupPolicyResponse = GetModelPackageGroupPolicyResponse.builder()
                .resourcePolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
                .build();

        when(proxyClient.client().getModelPackageGroupPolicy(any(GetModelPackageGroupPolicyRequest.class)))
                .thenReturn(getModelPackageGroupPolicyResponse);

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

        final DeleteTagsResponse deleteTagsResponse =
                DeleteTagsResponse.builder().build();
        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenReturn(deleteTagsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
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
    public void testUpdateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.UPDATE), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingResourcePolicy_PutPolicy() {
        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingResourcePolicy_DeletePolicy() {
        when(proxyClient.client().deleteModelPackageGroupPolicy(any(DeleteModelPackageGroupPolicyRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelpackagegroupNotFound_UpdatingResourcePolicy_PutPolicy() {
        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
                .message(CANNOT_FIND_MODEL_PACKAGE_GROUP_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelpackagegroupNotFound_UpdatingResourcePolicy_DeletePolicy() {
        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
                .message(CANNOT_FIND_MODEL_PACKAGE_GROUP_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().deleteModelPackageGroupPolicy(any(DeleteModelPackageGroupPolicyRequest.class)))
                .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingTags() {
        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithBothTagsAndResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingTags_AddTags() {
        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithBothTagsAndResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingTags_DeleteTags() {
        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelPackageGroupNotExists_UpdatingTags() {
        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
                .message(MODEL_PACKAGE_GROUP_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelPackageGroupNotExists_UpdatingTags_AddTags() {
        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(new ArrayList<>())
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
                .message(MODEL_PACKAGE_GROUP_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
                .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithBothTagsAndResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelPackageGroupNotExists_UpdatingTags_DeleteTags() {
        final PutModelPackageGroupPolicyResponse putModelPackageGroupPolicyResponse = PutModelPackageGroupPolicyResponse.builder()
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .build();

        when(proxyClient.client().putModelPackageGroupPolicy(any(PutModelPackageGroupPolicyRequest.class)))
                .thenReturn(putModelPackageGroupPolicyResponse);

        final ListTagsResponse listTagsResponse =
                ListTagsResponse.builder()
                        .tags(TEST_SDK_TAGS)
                        .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
                .message(MODEL_PACKAGE_GROUP_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackagegroup.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackagegroup.ResourceModel>builder()
                .desiredResourceState(getRequestResourceModelWithResourcePolicy())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                software.amazon.sagemaker.modelpackagegroup.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    private ResourceModel getRequestResourceModelWithResourcePolicy() {
        return ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
                .build();
    }

    private ResourceModel getRequestResourceModelWithBothTagsAndResourcePolicy() {
        return ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupPolicy(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT)
                .tags(TEST_CFN_MODEL_TAGS)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
                .build();
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .creationTime(TEST_CREATION_TIME.toString())
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                .modelPackageGroupStatus(ModelPackageGroupStatus.COMPLETED.toString())
                .build();
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.modelpackagegroup.UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
