package software.amazon.sagemaker.appimageconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppImageConfigResponse;
import software.amazon.awssdk.services.sagemaker.model.FileSystemConfig;
import software.amazon.awssdk.services.sagemaker.model.KernelGatewayImageConfig;
import software.amazon.awssdk.services.sagemaker.model.KernelSpec;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends software.amazon.sagemaker.appimageconfig.AbstractTestBase {

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
        final software.amazon.awssdk.services.sagemaker.model.KernelSpec kernelSpec = KernelSpec.builder()
                .name(TEST_KERNEL_NAME)
                .displayName(TEST_KERNEL_DISPLAY)
                .build();

        final software.amazon.awssdk.services.sagemaker.model.FileSystemConfig fileSystemConfig =
                FileSystemConfig.builder()
                        .defaultGid(TEST_DEFAULT_GID)
                        .defaultUid(TEST_DEFAULT_UID)
                        .mountPath(TEST_MOUNT_PATH)
                        .build();

        final software.amazon.awssdk.services.sagemaker.model.KernelGatewayImageConfig kernelGatewayImageConfig =
                KernelGatewayImageConfig.builder()
                        .kernelSpecs(Collections.singletonList(kernelSpec))
                        .fileSystemConfig(fileSystemConfig)
                        .build();

        final DescribeAppImageConfigResponse describeResponse = DescribeAppImageConfigResponse.builder()
                .appImageConfigArn(TEST_APP_IMAGE_CONFIG_ARN)
                .appImageConfigName(TEST_APP_IMAGE_CONFIG_NAME)
                .kernelGatewayImageConfig(kernelGatewayImageConfig)
                .build();

        when(proxyClient.client().describeAppImageConfig(any(DescribeAppImageConfigRequest.class)))
                .thenReturn(describeResponse);

        final software.amazon.sagemaker.appimageconfig.KernelSpec expectedKernelSpec =
                software.amazon.sagemaker.appimageconfig.KernelSpec.builder()
                        .name(TEST_KERNEL_NAME)
                        .displayName(TEST_KERNEL_DISPLAY)
                        .build();

        final software.amazon.sagemaker.appimageconfig.FileSystemConfig expectedFileSystemConfig =
                software.amazon.sagemaker.appimageconfig.FileSystemConfig.builder()
                        .defaultGid(TEST_DEFAULT_GID)
                        .defaultUid(TEST_DEFAULT_UID)
                        .mountPath(TEST_MOUNT_PATH)
                        .build();

        final software.amazon.sagemaker.appimageconfig.KernelGatewayImageConfig expectedKernelGatewayImageConfig =
                software.amazon.sagemaker.appimageconfig.KernelGatewayImageConfig.builder()
                .kernelSpecs(Collections.singletonList(expectedKernelSpec))
                .fileSystemConfig(expectedFileSystemConfig)
                .build();

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .appImageConfigArn(TEST_APP_IMAGE_CONFIG_ARN)
                .appImageConfigName(TEST_APP_IMAGE_CONFIG_NAME)
                .kernelGatewayImageConfig(expectedKernelGatewayImageConfig)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedResourceModel);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeAppImageConfig(any(DescribeAppImageConfigRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeAppImageConfig(any(DescribeAppImageConfigRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_AppImageConfigDoesNotExist_Fails() {
        final AwsServiceException resourceNotFoundException = AwsServiceException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().describeAppImageConfig(any(DescribeAppImageConfigRequest.class)))
                .thenThrow(resourceNotFoundException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeAppImageConfig(any(DescribeAppImageConfigRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_APP_IMAGE_CONFIG_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}