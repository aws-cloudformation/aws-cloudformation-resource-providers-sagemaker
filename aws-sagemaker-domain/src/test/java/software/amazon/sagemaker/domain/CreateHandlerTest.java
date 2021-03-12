package software.amazon.sagemaker.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DomainStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends software.amazon.sagemaker.domain.AbstractTestBase {

    private final ResourceModel REQUEST_MODEL = ResourceModel.builder()
            .domainName(TEST_DOMAIN_NAME)
            .build();

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
    public void testCreateHandler_SimpleSuccess() {
        final DescribeDomainResponse describeResponse = DescribeDomainResponse.builder()
                .domainName(TEST_DOMAIN_NAME)
                .domainId(TEST_DOMAIN_ID)
                .domainArn(TEST_DOMAIN_ARN)
                .url(TEST_URL)
                .homeEfsFileSystemId(TEST_EFS_ID)
                .singleSignOnManagedApplicationInstanceId(TEST_SSO_MANAGED_APP)
                .status(DomainStatus.IN_SERVICE)
                .build();

        final CreateDomainResponse createResponse = CreateDomainResponse.builder()
                .domainArn(TEST_DOMAIN_ARN)
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(describeResponse);
        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .domainId(TEST_DOMAIN_ID)
                .domainName(TEST_DOMAIN_NAME)
                .domainArn(TEST_DOMAIN_ARN)
                .url(TEST_URL)
                .homeEfsFileSystemId(TEST_EFS_ID)
                .singleSignOnManagedApplicationInstanceId(TEST_SSO_MANAGED_APP)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_InvalidRequestDomainId() {
        final ResourceModel invalidModel = ResourceModel.builder()
                .domainId(TEST_DOMAIN_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "The following property 'DomainId' is not allowed to configured."));
    }

    @Test
    public void testCreateHandler_InvalidRequestDomainArn() {
        final ResourceModel invalidModel = ResourceModel.builder()
                .domainArn(TEST_DOMAIN_ARN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "The following property 'DomainArn' is not allowed to configured."));
    }

    @Test
    public void testCreateHandler_InvalidRequestUrl() {
        final ResourceModel invalidModel = ResourceModel.builder()
                .url(TEST_URL)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "The following property 'Url' is not allowed to configured."));
    }

    @Test
    public void testCreateHandler_InvalidRequestDomainEFSId() {
        final ResourceModel invalidModel = ResourceModel.builder()
                .homeEfsFileSystemId(TEST_EFS_ID)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "The following property 'HomeEfsFileSystemId' is not allowed to configured."));
    }

    @Test
    public void testCreateHandler_InvalidRequestDomainSSO_Id() {
        final ResourceModel invalidModel = ResourceModel.builder()
                .singleSignOnManagedApplicationInstanceId(TEST_SSO_MANAGED_APP)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidModel)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                "The following property 'SingleSignOnManagedApplicationInstanceId' is not allowed to configured."));
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_ResourceAlreadyExists_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_DOMAIN_NAME));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, TEST_ERROR_MESSAGE));
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsErrorDetails awsErrorDetails =
                AwsErrorDetails.builder().errorCode("ValidationError").errorMessage(TEST_ERROR_MESSAGE).build();

        final AwsServiceException validationFailureException = SageMakerException.builder()
                .awsErrorDetails(awsErrorDetails)
                .build();

        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                TEST_ERROR_MESSAGE));
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_InService() {
        final DescribeDomainResponse firstDescribeResponse =
                DescribeDomainResponse.builder()
                        .status(DomainStatus.PENDING)
                        .build();

        final DescribeDomainResponse secondDescribeResponse =
                DescribeDomainResponse.builder()
                        .domainArn(TEST_DOMAIN_ARN)
                        .domainName(TEST_DOMAIN_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .url(TEST_URL)
                        .homeEfsFileSystemId(TEST_EFS_ID)
                        .singleSignOnManagedApplicationInstanceId(TEST_SSO_MANAGED_APP)
                        .status(DomainStatus.IN_SERVICE)
                        .build();

        final CreateDomainResponse createDomainResponse = CreateDomainResponse.builder()
                .domainArn(TEST_DOMAIN_ARN)
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenReturn(createDomainResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .domainArn(TEST_DOMAIN_ARN)
                .domainName(TEST_DOMAIN_NAME)
                .domainId(TEST_DOMAIN_ID)
                .url(TEST_URL)
                .homeEfsFileSystemId(TEST_EFS_ID)
                .singleSignOnManagedApplicationInstanceId(TEST_SSO_MANAGED_APP)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_VerifyStabilization_Failed() {
        final DescribeDomainResponse firstDescribeResponse =
                DescribeDomainResponse.builder()
                        .status(DomainStatus.PENDING)
                        .build();

        final DescribeDomainResponse secondDescribeResponse =
                DescribeDomainResponse.builder()
                        .status(DomainStatus.FAILED)
                        .build();

        final CreateDomainResponse createDomainResponse = CreateDomainResponse.builder()
                .domainArn(TEST_DOMAIN_ARN)
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().createDomain(any(CreateDomainRequest.class)))
                .thenReturn(createDomainResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(REQUEST_MODEL)
                .build();

        Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).
                isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(), ResourceModel.TYPE_NAME, TEST_DOMAIN_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}