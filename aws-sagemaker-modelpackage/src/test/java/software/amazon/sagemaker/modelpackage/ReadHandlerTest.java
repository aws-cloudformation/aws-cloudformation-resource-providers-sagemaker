package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
import software.amazon.awssdk.services.sagemaker.model.ModelApprovalStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);

        final ResourceModel expectedResourceModel = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
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
        verify(proxyClient.client()).describeModelPackage(any(DescribeModelPackageRequest.class));
    }

    @Test
    public void testReadHandler_WithEmptyTags() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL)
                .modelPackageStatus(ModelPackageStatus.COMPLETED)
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

        final ListTagsResponse listTagsResponse =
            ListTagsResponse.builder()
                .tags(new ArrayList<>())
                .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);

        final ResourceModel expectedResourceModel = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
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
        verify(proxyClient.client()).describeModelPackage(any(DescribeModelPackageRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
            .message(TEST_INTERNAL_ERROR_MESSAGE)
            .statusCode(500)
            .build();

        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(TEST_INTERNAL_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void testReadHandler_ModelPackageNotExist_Fails() {
        final AwsServiceException resourceNotExistexception = SageMakerException.builder()
            .message(MODEL_PACKAGE_NOT_EXISTS_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenThrow(resourceNotExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_NAME), exception.getMessage());
    }


    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.NotFound.getMessage(),
            ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_NAME), exception.getMessage());
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .build();
    }
}
