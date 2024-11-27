package software.amazon.sagemaker.modelpackage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.AddTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
import software.amazon.awssdk.services.sagemaker.model.ModelApprovalStatus;
import software.amazon.awssdk.services.sagemaker.model.UpdateModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateModelPackageResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import java.time.Duration;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    private static final String OPERATION = "UPDATE";

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
    public void testUpdateHandler_Fails_NoValidUpdate() {
        ResourceModel resourceModel = getRequestResourceModelVersioned();
        resourceModel.setModelPackageName(TEST_MODEL_PACKAGE_NAME);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(resourceModel)
            .build();

        Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertEquals("Invalid request provided: Either ModelPackageName or ModelPackageGroupName should be present to update ModelPackage for testModelPackageArn."
            , exception.getMessage());
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_VersionedArn() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL)
                .lastModifiedTime(TEST_CREATION_TIME)
                .build();

        final UpdateModelPackageResponse updateModelPackageResponse = UpdateModelPackageResponse.builder()
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .build();

        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);
        when(proxyClient.client().updateModelPackage(any(UpdateModelPackageRequest.class)))
            .thenReturn(updateModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelVersioned())
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .lastModifiedTime(TEST_CREATION_TIME.toString())
            .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_SimpleSuccess_AddTags() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

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
            .desiredResourceState(getRequestResourceModelWithTags())
            .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
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
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

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
            .desiredResourceState(getRequestResourceModelNonVersioned())
            .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .build();

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedModelFromResponse, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }


    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingTags() {
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackage.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackage.ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelWithTags())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            software.amazon.sagemaker.modelpackage.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingTags_AddTags() {
        final ListTagsResponse listTagsResponse =
            ListTagsResponse.builder()
                .tags(new ArrayList<>())
                .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);

        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackage.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackage.ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelWithTags())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            software.amazon.sagemaker.modelpackage.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException_UpdatingTags_DeleteTags() {
        final ListTagsResponse listTagsResponse =
            ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);

        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackage.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackage.ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelNonVersioned())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            software.amazon.sagemaker.modelpackage.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelPackageNotExists_UpdatingTags() {
        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
            .message(MODEL_PACKAGE_NOT_EXISTS_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackage.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackage.ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelNonVersioned())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            software.amazon.sagemaker.modelpackage.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelPackageNotExists_UpdatingTags_AddTags() {
        final ListTagsResponse listTagsResponse =
            ListTagsResponse.builder()
                .tags(new ArrayList<>())
                .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);

        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
            .message(MODEL_PACKAGE_NOT_EXISTS_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().addTags(any(AddTagsRequest.class)))
            .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackage.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackage.ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelWithTags())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            software.amazon.sagemaker.modelpackage.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ModelPackageNotExists_UpdatingTags_DeleteTags() {
        final ListTagsResponse listTagsResponse =
            ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);

        final AwsServiceException resourceNotExistsException = SageMakerException.builder()
            .message(MODEL_PACKAGE_NOT_EXISTS_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
            .thenThrow(resourceNotExistsException);

        final ResourceHandlerRequest<software.amazon.sagemaker.modelpackage.ResourceModel> request = ResourceHandlerRequest.<software.amazon.sagemaker.modelpackage.ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelNonVersioned())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            software.amazon.sagemaker.modelpackage.ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }
    
    
    @Test
    public void testUpdateHandler_ServiceInternalException() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

        final Exception serviceInternalException = SageMakerException.builder()
            .message(TEST_INTERNAL_ERROR_MESSAGE)
            .statusCode(500)
            .build();

        when(proxyClient.client().updateModelPackage(any(UpdateModelPackageRequest.class)))
            .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelVersioned())
            .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(TEST_INTERNAL_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

        when(proxyClient.client().updateModelPackage(any(UpdateModelPackageRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelVersioned())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
            ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN));
    }

    @Test
    public void testUpdateHandler_ResourceLimitExceededException() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);


        when(proxyClient.client().updateModelPackage(any(UpdateModelPackageRequest.class)))
            .thenThrow(ResourceLimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModelVersioned())
            .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
            OPERATION));
    }

    private ResourceModel getRequestResourceModelVersioned() {
        return ResourceModel.builder()
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageGroupName(TEST_MODEL_PACKAGE_NAME)
            .modelApprovalStatus(ModelApprovalStatus.APPROVED.toString())
            .approvalDescription("testDesc")
            .build();
    }

    private ResourceModel getRequestResourceModelNonVersioned() {
        return ResourceModel.builder()
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelApprovalStatus(ModelApprovalStatus.APPROVED.toString())
            .approvalDescription("testDesc")
            .build();
    }

    private ResourceModel getRequestResourceModelWithTags() {
        return ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .tags(TEST_CFN_MODEL_TAGS)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.modelpackage.UpdateHandler handler = new software.amazon.sagemaker.modelpackage.UpdateHandler();
        return handler.handleRequest(proxy, request, new software.amazon.sagemaker.modelpackage.CallbackContext(), proxyClient, logger);
    }
}
