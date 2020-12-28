package software.amazon.sagemaker.modelpackagegroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageGroupStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        final DeleteModelPackageGroupResponse deleteModelPackageGroupResponse = DeleteModelPackageGroupResponse.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteModelPackageGroup(any(DeleteModelPackageGroupRequest.class)))
                .thenReturn(deleteModelPackageGroupResponse);


        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

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

        when(proxyClient.client().deleteModelPackageGroup(any(DeleteModelPackageGroupRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.DELETE), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_ModelPackageGroupDoesNotExists_Fails() {
        final AwsServiceException resourceNotExistexception = SageMakerException.builder()
                .message(MODEL_PACKAGE_GROUP_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().deleteModelPackageGroup(any(DeleteModelPackageGroupRequest.class)))
                .thenThrow(resourceNotExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_ResourceNotFoundException_Fails() {
        when(proxyClient.client().deleteModelPackageGroup(any(DeleteModelPackageGroupRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete_WithResourceNotFoundException() {
        final DescribeModelPackageGroupResponse firstDescribeResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.DELETING)
                        .build();

        final DeleteModelPackageGroupResponse deleteModelPackageGroupResponse = DeleteModelPackageGroupResponse.builder()
                .build();

        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(firstDescribeResponse).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteModelPackageGroup(any(DeleteModelPackageGroupRequest.class)))
                .thenReturn(deleteModelPackageGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete_WithModelPackageGroupNotExist() {
        final DescribeModelPackageGroupResponse firstDescribeResponse =
                DescribeModelPackageGroupResponse.builder()
                        .creationTime(TEST_CREATION_TIME)
                        .modelPackageGroupArn(TEST_MODEL_PACKAGE_GROUP_ARN)
                        .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                        .modelPackageGroupStatus(ModelPackageGroupStatus.DELETING)
                        .build();

        final AwsServiceException resourceNotExistexception = SageMakerException.builder()
                .message(MODEL_PACKAGE_GROUP_NOT_EXISTS_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        final DeleteModelPackageGroupResponse deleteModelPackageGroupResponse = DeleteModelPackageGroupResponse.builder()
                .build();

        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(firstDescribeResponse).thenThrow(resourceNotExistexception);
        when(proxyClient.client().deleteModelPackageGroup(any(DeleteModelPackageGroupRequest.class)))
                .thenReturn(deleteModelPackageGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_ResourceNotDeleted() {
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

        final DeleteModelPackageGroupResponse deleteModelPackageGroupResponse = DeleteModelPackageGroupResponse.builder()
                .build();

        when(proxyClient.client().describeModelPackageGroup(any(DescribeModelPackageGroupRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteModelPackageGroup(any(DeleteModelPackageGroupRequest.class)))
                .thenReturn(deleteModelPackageGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Delete stabilizing of model package group: " + TEST_MODEL_PACKAGE_GROUP_NAME), exception.getMessage());
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .modelPackageGroupName(TEST_MODEL_PACKAGE_GROUP_NAME)
                .build();
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackagegroup.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.modelpackagegroup.DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
