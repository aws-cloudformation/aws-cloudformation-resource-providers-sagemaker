package software.amazon.sagemaker.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DomainStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
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
public class DeleteHandlerTest extends software.amazon.sagemaker.domain.AbstractTestBase {

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
        final DeleteDomainResponse deleteDomainResponse = DeleteDomainResponse.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteDomain(any(DeleteDomainRequest.class)))
                .thenReturn(deleteDomainResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo((OperationStatus.SUCCESS));
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void testDeleteHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        when(proxyClient.client().deleteDomain(any(DeleteDomainRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.DELETE));
    }

    @Test
    public void testDeleteHandler_ResourceInUse_Fails() {
        when(proxyClient.client().deleteDomain(any(DeleteDomainRequest.class)))
                .thenThrow(ResourceInUseException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows(CfnResourceConflictException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ResourceConflict.getMessage(),
                ResourceModel.TYPE_NAME, TEST_DOMAIN_ID, null));
    }

    @Test
    public void testDeleteHandler_ResourceDoesNotExists_Fails() {
        when(proxyClient.client().deleteDomain(any(DeleteDomainRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_DOMAIN_ID));
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete() {
        final DescribeDomainResponse firstDescribeResponse =
                DescribeDomainResponse.builder()
                        .domainName(TEST_DOMAIN_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .status(DomainStatus.DELETING)
                        .build();

        final DeleteDomainResponse deleteDomainResponse = DeleteDomainResponse.builder()
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(firstDescribeResponse).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteDomain(any(DeleteDomainRequest.class)))
                .thenReturn(deleteDomainResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_ResourceNotDeleted() {
        final DescribeDomainResponse firstDescribeResponse =DescribeDomainResponse.builder()
                        .status(DomainStatus.PENDING)
                        .build();

        final DescribeDomainResponse secondDescribeResponse = DescribeDomainResponse.builder()
                .status(DomainStatus.FAILED)
                        .build();

        final DeleteDomainResponse deleteDomainResponse = DeleteDomainResponse.builder()
                .build();

        when(proxyClient.client().describeDomain(any(DescribeDomainRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().deleteDomain(any(DeleteDomainRequest.class)))
                .thenReturn(deleteDomainResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getPostCreationResourceModel())
                .build();

        Exception exception = assertThrows( CfnNotStabilizedException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, getPostCreationResourceModel().getPrimaryIdentifier()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
