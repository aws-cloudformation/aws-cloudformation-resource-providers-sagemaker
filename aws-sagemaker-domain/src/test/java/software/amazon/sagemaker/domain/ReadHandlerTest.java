package software.amazon.sagemaker.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CustomImage;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.JupyterServerAppSettings;
import software.amazon.awssdk.services.sagemaker.model.KernelGatewayAppSettings;
import software.amazon.awssdk.services.sagemaker.model.ListDomainsRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.ResourceSpec;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.SharingSettings;
import software.amazon.awssdk.services.sagemaker.model.UserSettings;
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
public class ReadHandlerTest extends software.amazon.sagemaker.domain.AbstractTestBase {

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
        final SharingSettings sharingSettings = SharingSettings.builder()
                .notebookOutputOption(TEST_NB_OUTPUT)
                .s3KmsKeyId(TEST_S3_KMS)
                .s3OutputPath(TEST_S3_OUTPUT)
                .build();

        final CustomImage customImage = CustomImage.builder()
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

        final UserSettings userSettings = UserSettings.builder()
                .sharingSettings(sharingSettings)
                .securityGroups(TEST_SECURITY_GROUP)
                .executionRole(TEST_ROLE)
                .kernelGatewayAppSettings(kernelGatewayAppSettings)
                .jupyterServerAppSettings(jupyterServerAppSettings)
                .build();

        final DescribeDomainResponse describeResponse = DescribeDomainResponse.builder()
                .domainArn(TEST_DOMAIN_ARN)
                .domainName(TEST_DOMAIN_NAME)
                .domainId(TEST_DOMAIN_ID)
                .appNetworkAccessType(TEST_APP_NETWORK_TYPE)
                .authMode(TEST_AUTH_MODE)
                .defaultUserSettings(userSettings)
                .homeEfsFileSystemId(TEST_EFS_ID)
                .singleSignOnManagedApplicationInstanceId(TEST_SSO_MANAGED_APP)
                .subnetIds(TEST_SUBNET_ID)
                .status(TEST_STATUS)
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(describeResponse);

        final software.amazon.sagemaker.domain.ResourceSpec expectedResourceSpec =
                software.amazon.sagemaker.domain.ResourceSpec.builder()
                        .instanceType(TEST_INSTANCE_TYPE)
                        .sageMakerImageArn(TEST_IMAGE_ARN)
                        .sageMakerImageVersionArn(TEST_IMAGE_VERSION_ARN)
                        .build();

        final software.amazon.sagemaker.domain.SharingSettings expectedSharingSettings =
                software.amazon.sagemaker.domain.SharingSettings.builder()
                        .notebookOutputOption(TEST_NB_OUTPUT)
                        .s3KmsKeyId(TEST_S3_KMS)
                        .s3OutputPath(TEST_S3_OUTPUT)
                        .build();

        final software.amazon.sagemaker.domain.CustomImage expectedCustomImage =
                software.amazon.sagemaker.domain.CustomImage.builder()
                .imageName(TEST_IMAGE_NAME)
                .imageVersionNumber(TEST_IMAGE_VERSION_NUMBER)
                .appImageConfigName(TEST_APP_IMAGE_CONFIG_NAME)
                .build();

        final software.amazon.sagemaker.domain.KernelGatewayAppSettings expectedKernelGatewayAppSettings =
                software.amazon.sagemaker.domain.KernelGatewayAppSettings.builder()
                .customImages(Collections.singletonList(expectedCustomImage))
                .defaultResourceSpec(expectedResourceSpec)
                .build();

        final software.amazon.sagemaker.domain.JupyterServerAppSettings expectedJupyterServerAppSettings =
                software.amazon.sagemaker.domain.JupyterServerAppSettings.builder()
                .defaultResourceSpec(expectedResourceSpec)
                .build();

        final software.amazon.sagemaker.domain.UserSettings expectedUserSettings =
                software.amazon.sagemaker.domain.UserSettings.builder()
                        .sharingSettings(expectedSharingSettings)
                        .securityGroups(Collections.singletonList(TEST_SECURITY_GROUP))
                        .executionRole(TEST_ROLE)
                        .kernelGatewayAppSettings(expectedKernelGatewayAppSettings)
                        .jupyterServerAppSettings(expectedJupyterServerAppSettings)
                        .build();

        final ResourceModel expectedResourceModel = ResourceModel.builder()
                .domainArn(TEST_DOMAIN_ARN)
                .domainName(TEST_DOMAIN_NAME)
                .domainId(TEST_DOMAIN_ID)
                .appNetworkAccessType(TEST_APP_NETWORK_TYPE)
                .authMode(TEST_AUTH_MODE)
                .defaultUserSettings(expectedUserSettings)
                .subnetIds(Collections.singletonList(TEST_SUBNET_ID))
                .homeEfsFileSystemId(TEST_EFS_ID)
                .singleSignOnManagedApplicationInstanceId(TEST_SSO_MANAGED_APP)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedResourceModel);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeDomain(any(DescribeDomainRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_DomainDoesNotExist_Fails() {
        final AwsServiceException resourceNotFoundException = AwsServiceException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenThrow(resourceNotFoundException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_DOMAIN_ID));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}