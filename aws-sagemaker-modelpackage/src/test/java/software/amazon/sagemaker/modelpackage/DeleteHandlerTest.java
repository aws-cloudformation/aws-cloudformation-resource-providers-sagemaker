package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
import software.amazon.awssdk.services.sagemaker.model.ModelApprovalStatus;
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
        final DeleteModelPackageResponse deleteModelPackageResponse = DeleteModelPackageResponse.builder()
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteModelPackage(any(DeleteModelPackageRequest.class)))
            .thenReturn(deleteModelPackageResponse);


        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);

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
            .message(TEST_INTERNAL_ERROR_MESSAGE)
            .statusCode(500)
            .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        when(proxyClient.client().deleteModelPackage(any(DeleteModelPackageRequest.class)))
            .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(TEST_INTERNAL_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void testDeleteHandler_ModelPackageDoesNotExists_Fails() {
        final AwsServiceException resourceNotExistexception = SageMakerException.builder()
            .message(MODEL_PACKAGE_NOT_EXISTS_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().deleteModelPackage(any(DeleteModelPackageRequest.class)))
            .thenThrow(resourceNotExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_ResourceNotFoundException_Fails() {
        when(proxyClient.client().deleteModelPackage(any(DeleteModelPackageRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete_WithResourceNotFoundException() {
        final DescribeModelPackageResponse firstDescribeResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageStatus(ModelPackageStatus.DELETING)
                .build();

        final DeleteModelPackageResponse deleteModelPackageResponse = DeleteModelPackageResponse.builder()
            .build();

        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(firstDescribeResponse).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteModelPackage(any(DeleteModelPackageRequest.class)))
            .thenReturn(deleteModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete_WithModelPackageNotExist() {
        final software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse firstDescribeResponse =
            software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageStatus(ModelPackageStatus.DELETING)
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL)
                .build();

        final AwsServiceException resourceNotExistexception = software.amazon.awssdk.services.sagemaker.model.SageMakerException.builder()
            .message(MODEL_PACKAGE_NOT_EXISTS_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        final software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageResponse deleteModelPackageResponse = software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageResponse.builder()
            .build();

        when(proxyClient.client().describeModelPackage(any(software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageRequest.class)))
            .thenReturn(firstDescribeResponse).thenThrow(resourceNotExistexception);
        when(proxyClient.client().deleteModelPackage(any(software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageRequest.class)))
            .thenReturn(deleteModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);
        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_ResourceNotDeleted() {
        final DescribeModelPackageResponse firstDescribeResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageStatus(ModelPackageStatus.PENDING)
                .build();

        final DescribeModelPackageResponse secondDescribeResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageStatus(ModelPackageStatus.FAILED)
                .build();

        final DeleteModelPackageResponse deleteModelPackageResponse = DeleteModelPackageResponse.builder()
            .build();

        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteModelPackage(any(DeleteModelPackageRequest.class)))
            .thenReturn(deleteModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
            "Delete stabilizing of model package: " + TEST_MODEL_PACKAGE_ARN), exception.getMessage());
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
            .modelPackageName(TEST_MODEL_PACKAGE_ARN)
            .build();
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.modelpackage.DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
