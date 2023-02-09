package software.amazon.sagemaker.space;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeSpaceRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeSpaceResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.UpdateSpaceRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateSpaceResponse;
import software.amazon.awssdk.services.sagemaker.model.SpaceStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends software.amazon.sagemaker.space.AbstractTestBase {

    private static final String OPERATION = "SageMaker::UpdateSpace";

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
        final DescribeSpaceResponse describeSpaceResponse =
                DescribeSpaceResponse.builder()
                        .spaceName(TEST_SPACE_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .creationTime(TEST_TIME)
                        .lastModifiedTime(TEST_TIME)
                        .status(SpaceStatus.IN_SERVICE)
                        .build();

        final UpdateSpaceResponse updateSpaceResponse =
                UpdateSpaceResponse
                        .builder()
                        .spaceArn(TEST_SPACE_ARN)
                        .build();

        when(proxyClient.client().describeSpace(any(DescribeSpaceRequest.class)))
                .thenReturn(describeSpaceResponse);
        when(proxyClient.client().updateSpace(any(UpdateSpaceRequest.class)))
                .thenReturn(updateSpaceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .spaceArn(TEST_SPACE_ARN)
                .spaceName(TEST_SPACE_NAME)
                .domainId(TEST_DOMAIN_ID)
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

        when(proxyClient.client().updateSpace(any(UpdateSpaceRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.UPDATE.toString()));
    }

    @Test
    public void testUpdateHandler_ResourceNotFoundException() {
        when(proxyClient.client().updateSpace(any(UpdateSpaceRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_SPACE_NAME));
    }

    @Test
    public void testUpdateHandler_ResourceLimitExceededException() {
        when(proxyClient.client().updateSpace(any(UpdateSpaceRequest.class)))
                .thenThrow(ResourceLimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, null));
    }

    @Test
    public void testUpdateHandler_VerifyStabilization_SuccessfulUpdate() {
        final DescribeSpaceResponse firstDescribeResponse =
                DescribeSpaceResponse.builder()
                        .status(SpaceStatus.UPDATING)
                        .build();

        final DescribeSpaceResponse secondDescribeResponse =
                DescribeSpaceResponse.builder()
                        .status(SpaceStatus.IN_SERVICE)
                        .build();

        final UpdateSpaceResponse updateSpaceResponse =
                UpdateSpaceResponse.builder()
                        .spaceArn(TEST_SPACE_ARN)
                        .build();

        when(proxyClient.client().describeSpace(any(DescribeSpaceRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateSpace(any(UpdateSpaceRequest.class)))
                .thenReturn(updateSpaceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .spaceArn(TEST_SPACE_ARN)
                .spaceName(TEST_SPACE_NAME)
                .domainId(TEST_DOMAIN_ID)
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
        final DescribeSpaceResponse firstDescribeResponse =
                DescribeSpaceResponse.builder()
                        .spaceName(TEST_SPACE_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .status(SpaceStatus.UPDATING)
                        .build();

        final DescribeSpaceResponse secondDescribeResponse =
                DescribeSpaceResponse.builder()
                        .spaceName(TEST_SPACE_NAME)
                        .domainId(TEST_DOMAIN_ID)
                        .status(SpaceStatus.UPDATE_FAILED)
                        .build();

        final UpdateSpaceResponse updateSpaceResponse = UpdateSpaceResponse.builder()
                .build();

        when(proxyClient.client().describeSpace(any(DescribeSpaceRequest.class)))
                .thenReturn(firstDescribeResponse).thenReturn(secondDescribeResponse);
        when(proxyClient.client().updateSpace(any(UpdateSpaceRequest.class)))
                .thenReturn(updateSpaceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Stabilizing during update of " + getRequestResourceModel().getPrimaryIdentifier()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final UpdateHandler handler = new UpdateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}