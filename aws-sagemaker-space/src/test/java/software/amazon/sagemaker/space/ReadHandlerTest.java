package software.amazon.sagemaker.space;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CustomImage;
import software.amazon.awssdk.services.sagemaker.model.DescribeSpaceRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeSpaceResponse;
import software.amazon.awssdk.services.sagemaker.model.JupyterServerAppSettings;
import software.amazon.awssdk.services.sagemaker.model.KernelGatewayAppSettings;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.ResourceSpec;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.SpaceSettings;
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
public class ReadHandlerTest extends software.amazon.sagemaker.space.AbstractTestBase {

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

        final software.amazon.awssdk.services.sagemaker.model.CustomImage customImage = CustomImage.builder()
                .imageName(TEST_IMAGE_NAME)
                .imageVersionNumber(TEST_IMAGE_VERSION_NUMBER)
                .appImageConfigName(TEST_APP_IMAGE_CONFIG_NAME)
                .build();

        final ResourceSpec resourceSpec = ResourceSpec.builder()
                .instanceType(TEST_INSTANCE_TYPE)
                .sageMakerImageArn(TEST_IMAGE_ARN)
                .sageMakerImageVersionArn(TEST_IMAGE_VERSION_ARN)
                .build();

        final KernelGatewayAppSettings kernelGatewayAppSettings = KernelGatewayAppSettings.builder()
                .customImages(customImage)
                .defaultResourceSpec(resourceSpec)
                .build();

        final JupyterServerAppSettings jupyterServerAppSettings = JupyterServerAppSettings.builder()
                .defaultResourceSpec(resourceSpec)
                .build();

        final SpaceSettings spaceSettings = SpaceSettings.builder()
                .kernelGatewayAppSettings(kernelGatewayAppSettings)
                .jupyterServerAppSettings(jupyterServerAppSettings)
                .build();

        final DescribeSpaceResponse describeResponse = DescribeSpaceResponse.builder()
                .spaceArn(TEST_SPACE_ARN)
                .spaceName(TEST_SPACE_NAME)
                .domainId(TEST_DOMAIN_ID)
                .spaceSettings(spaceSettings)
                .creationTime(TEST_TIME)
                .lastModifiedTime(TEST_TIME)
                .failureReason(TEST_FAILURE_REASON)
                .status(TEST_STATUS)
                .build();

        when(proxyClient.client().describeSpace(any(DescribeSpaceRequest.class)))
                .thenReturn(describeResponse);

        final software.amazon.sagemaker.space.ResourceSpec expectedResourceSpec =
                software.amazon.sagemaker.space.ResourceSpec.builder()
                        .instanceType(TEST_INSTANCE_TYPE)
                        .sageMakerImageArn(TEST_IMAGE_ARN)
                        .sageMakerImageVersionArn(TEST_IMAGE_VERSION_ARN)
                        .build();

        final software.amazon.sagemaker.space.CustomImage expectedCustomImage =
                software.amazon.sagemaker.space.CustomImage.builder()
                .imageName(TEST_IMAGE_NAME)
                .imageVersionNumber(TEST_IMAGE_VERSION_NUMBER)
                .appImageConfigName(TEST_APP_IMAGE_CONFIG_NAME)
                .build();

        final software.amazon.sagemaker.space.KernelGatewayAppSettings expectedKernelGatewayAppSettings =
                software.amazon.sagemaker.space.KernelGatewayAppSettings.builder()
                .customImages(Collections.singletonList(expectedCustomImage))
                .defaultResourceSpec(expectedResourceSpec)
                .build();

        final software.amazon.sagemaker.space.JupyterServerAppSettings expectedJupyterServerAppSettings =
                software.amazon.sagemaker.space.JupyterServerAppSettings.builder()
                .defaultResourceSpec(expectedResourceSpec)
                .build();

        final software.amazon.sagemaker.space.SpaceSettings expectedSpaceSettings =
                software.amazon.sagemaker.space.SpaceSettings.builder()
                        .kernelGatewayAppSettings(expectedKernelGatewayAppSettings)
                        .jupyterServerAppSettings(expectedJupyterServerAppSettings)
                        .build();

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .spaceArn(TEST_SPACE_ARN)
                .spaceName(TEST_SPACE_NAME)
                .domainId(TEST_DOMAIN_ID)
                .spaceSettings(expectedSpaceSettings)
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
        verify(proxyClient.client()).describeSpace(any(DescribeSpaceRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeSpace(any(DescribeSpaceRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_SpaceDoesNotExist_Fails() {
        final AwsServiceException resourceNotFoundException = AwsServiceException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().describeSpace(any(DescribeSpaceRequest.class)))
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
        when(proxyClient.client().describeSpace(any(DescribeSpaceRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_SPACE_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}