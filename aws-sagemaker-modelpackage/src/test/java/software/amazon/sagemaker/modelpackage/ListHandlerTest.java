package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageSummary;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackagesResponse;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackagesRequest;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.OperationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        final ModelPackageSummary modelPackageSummary = ModelPackageSummary.builder()
            .creationTime(TEST_CREATION_TIME)
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageStatus(ModelPackageStatus.COMPLETED)
            .build();

        final ListModelPackagesResponse listModelPackagesResponse =
            ListModelPackagesResponse.builder()
                .modelPackageSummaryList(modelPackageSummary)
                .nextToken(TEST_TOKEN)
                .build();

        when(proxyClient.client().listModelPackages(any(ListModelPackagesRequest.class)))
            .thenReturn(listModelPackagesResponse);

        final ResourceModel expectedResourceModel = ResourceModel.builder()
            .creationTime(TEST_CREATION_TIME.toString())
            .modelPackageArn(TEST_MODEL_PACKAGE_ARN)
            .modelPackageName(TEST_MODEL_PACKAGE_NAME)
            .modelPackageStatus(ModelPackageStatus.COMPLETED.toString())
            .build();

        List<ResourceModel> expectedModels = new ArrayList<ResourceModel>();
        expectedModels.add(expectedResourceModel);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);

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
    public void testListHandler_SimpleSuccess_NoModelPackageExist() {
        final ListModelPackagesResponse listModelPackagesResponse =
            ListModelPackagesResponse.builder()
                .modelPackageSummaryList(Collections.emptyList())
                .nextToken(null)
                .build();

        when(proxyClient.client().listModelPackages(any(ListModelPackagesRequest.class)))
            .thenReturn(listModelPackagesResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();
        final ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> response = invokeHandleRequest(request);

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
        AwsServiceException ex = SageMakerException.builder()
            .message(TEST_INTERNAL_ERROR_MESSAGE)
            .statusCode(500)
            .build();

        when(proxyClient.client().listModelPackages(any(ListModelPackagesRequest.class)))
            .thenThrow(ex);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getRequestResourceModel())
            .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertEquals(TEST_INTERNAL_ERROR_MESSAGE, exception.getMessage());
    }

    private ProgressEvent<ResourceModel, software.amazon.sagemaker.modelpackage.CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final software.amazon.sagemaker.modelpackage.ListHandler handler = new ListHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder().build();
    }

}
