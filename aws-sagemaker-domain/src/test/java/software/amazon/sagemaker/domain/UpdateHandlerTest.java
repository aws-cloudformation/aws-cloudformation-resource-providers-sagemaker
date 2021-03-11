package software.amazon.sagemaker.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DomainStatus;
import software.amazon.awssdk.services.sagemaker.model.ListDomainsRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.UpdateDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateDomainResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends software.amazon.sagemaker.domain.AbstractTestBase {

    private static final String OPERATION = "SageMaker::UpdateDomain";

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
    public void testUpdateHandler_SimpleSuccess() {
        final DescribeDomainResponse describeDomainResponse =
                DescribeDomainResponse.builder()
                        .domainArn(TEST_DOMAIN_ARN)
                        .status(DomainStatus.IN_SERVICE)
                        .build();

        final UpdateDomainResponse updateDomainResponse = UpdateDomainResponse.builder().build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(describeDomainResponse);
        when(proxyClient.client().updateDomain(any(UpdateDomainRequest.class)))
                .thenReturn(updateDomainResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .domainId(TEST_DOMAIN_ID)
                .domainArn(TEST_DOMAIN_ARN)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        when(proxyClient.client().updateDomain(any(UpdateDomainRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.UPDATE.toString()));
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException() {
        when(proxyClient.client().updateDomain(any(UpdateDomainRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_DOMAIN_ID));
    }

    @Test
    public void testUpdateHandler_ResourceLimitExceededException() {
        when(proxyClient.client().updateDomain(any(UpdateDomainRequest.class)))
                .thenThrow(ResourceLimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, null));
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_SuccessfulUpdate() {
        final DescribeDomainResponse firstDescribeResponse =
                DescribeDomainResponse.builder()
                        .status(DomainStatus.UPDATING)
                        .build();

        final DescribeDomainResponse secondDescribeResponse =
                DescribeDomainResponse.builder()
                        .domainArn(TEST_DOMAIN_ARN)
                        .status(DomainStatus.IN_SERVICE)
                        .build();

        final UpdateDomainResponse updateDomainResponse = UpdateDomainResponse.builder().build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateDomain(any(UpdateDomainRequest.class)))
                .thenReturn(updateDomainResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .domainId(TEST_DOMAIN_ID)
                .domainArn(TEST_DOMAIN_ARN)
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void  testUpdateHandler_VerifyStabilization_Failed() {
        final DescribeDomainResponse firstDescribeResponse =
                DescribeDomainResponse.builder()
                        .status(DomainStatus.UPDATING)
                        .build();

        final DescribeDomainResponse secondDescribeResponse =
                DescribeDomainResponse.builder()
                        .status(DomainStatus.UPDATE_FAILED)
                        .build();

        final UpdateDomainResponse updateDomainResponse = UpdateDomainResponse.builder()
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateDomain(any(UpdateDomainRequest.class)))
                .thenReturn(updateDomainResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during update of " + request.getDesiredResourceState().getPrimaryIdentifier()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}