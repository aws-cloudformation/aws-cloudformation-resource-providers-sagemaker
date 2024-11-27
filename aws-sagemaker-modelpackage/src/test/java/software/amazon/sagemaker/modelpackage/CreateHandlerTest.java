package software.amazon.sagemaker.modelpackage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
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
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
import software.amazon.awssdk.services.sagemaker.model.ModelApprovalStatus;
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

    private final ResourceModel requestModelWithTags = ResourceModel.builder()
        .creationTime(TEST_CREATION_TIME.toString())
        .modelPackageName(TEST_MODEL_PACKAGE_NAME)
        .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
        .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
        .tags(TEST_CFN_MODEL_TAGS)
        .build();

    private final ResourceModel requestModel = ResourceModel.builder()
        .creationTime(TEST_CREATION_TIME.toString())
        .modelPackageName(TEST_MODEL_PACKAGE_NAME)
        .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
        .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
        .build();

    private final ResourceModel requestModelVersioned = ResourceModel.builder()
        .creationTime(TEST_CREATION_TIME.toString())
        .modelPackageGroupName(TEST_MODEL_PACKAGE_NAME)
        .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
        .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
        .build();

    private final ResourceModel requestModelVersionedWithTags = ResourceModel.builder()
        .creationTime(TEST_CREATION_TIME.toString())
        .modelPackageGroupName(TEST_MODEL_PACKAGE_NAME)
        .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
        .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
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
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse).thenReturn(describeModelPackageResponse);

        final ListTagsResponse listTagsResponse =
            ListTagsResponse.builder()
                .tags(new java.util.ArrayList<>())
                .build();
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);


        final CreateModelPackageResponse createModelPackageResponse = CreateModelPackageResponse.builder()
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .build();
        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenReturn(createModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
            .build();

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedModelFromResponse, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testCreateHandler_SimpleSuccess_Tags() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageName(TEST_MODEL_PACKAGE_NAME)
                .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

        final ListTagsResponse listTagsResponse =
            ListTagsResponse.builder()
                .tags(TEST_SDK_TAGS)
                .build();

        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
            .thenReturn(listTagsResponse);

        final CreateModelPackageResponse createModelPackageResponse = CreateModelPackageResponse.builder()
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .build();
        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenReturn(createModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModelWithTags)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
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
    public void testCreateHandler_SimpleSuccess_versioned() {
        final DescribeModelPackageResponse describeModelPackageResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(describeModelPackageResponse);

        final CreateModelPackageResponse createModelPackageResponse = CreateModelPackageResponse.builder()
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .build();
        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenReturn(createModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModelVersioned)
            .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
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
            .message(TEST_INTERNAL_ERROR_MESSAGE)
            .statusCode(500)
            .build();

        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(TEST_INTERNAL_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void testCreateHandler_ModelPackageAlreadyExists_Fails() {
        final ResourceInUseException resourceExistexception = ResourceInUseException.builder()
            .message(MODEL_PACKAGE_ALREADY_EXISTS_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenThrow(resourceExistexception);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
            ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_NAME), exception.getMessage());
    }

    @Test
    public void testCreateHandler_ResourceInUseException_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
            .message(TEST_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertEquals(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
            ResourceModel.TYPE_NAME, TEST_MODEL_PACKAGE_NAME), exception.getMessage());
    }
    
    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
            .message(TEST_ERROR_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(TEST_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsServiceException validationFailureException = SageMakerException.builder()
            .message(TEST_VALIDATION_FAILURE_MESSAGE)
            .statusCode(400)
            .build();

        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertEquals(TEST_VALIDATION_FAILURE_MESSAGE, exception.getMessage());
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
            .statusCode(400)
            .build();

        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
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
        final DescribeModelPackageResponse firstDescribeResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.PENDING.toString())
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
                .build();

        final DescribeModelPackageResponse secondDescribeResponse =
            DescribeModelPackageResponse.builder()
                .creationTime(TEST_CREATION_TIME)
                .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
                .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
                .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
                .build();
        when(proxyClient.client().describeModelPackage(any(DescribeModelPackageRequest.class)))
            .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);

        final CreateModelPackageResponse createModelPackageResponse = CreateModelPackageResponse.builder()
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .build();

        when(proxyClient.client().createModelPackage(any(CreateModelPackageRequest.class)))
            .thenReturn(createModelPackageResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(requestModel)
            .build();

        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .modelApprovalStatus(ModelApprovalStatus.PENDING_MANUAL_APPROVAL.toString())
            .build();

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(expectedModelFromResponse, response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.modelpackage.CreateHandler handler = new software.amazon.sagemaker.modelpackage.CreateHandler();
        return handler.handleRequest(proxy, request, new software.amazon.sagemaker.modelpackage.CallbackContext(), proxyClient, logger);
    }

}
