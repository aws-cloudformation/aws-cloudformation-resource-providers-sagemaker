package software.amazon.sagemaker.featuregroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.FeatureGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
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
public class DeleteHandlerTest extends AbstractTestBase {

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
        final DeleteFeatureGroupResponse deleteFeatureGroupResponse = DeleteFeatureGroupResponse.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteFeatureGroup(any(DeleteFeatureGroupRequest.class)))
                .thenReturn(deleteFeatureGroupResponse);


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
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .errorCode("InternalError")
                        .build())
                .statusCode(500)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        when(proxyClient.client().deleteFeatureGroup(any(DeleteFeatureGroupRequest.class)))
                .thenThrow(serviceInternalException);

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                serviceInternalException.awsErrorDetails().errorMessage()));
    }

    @Test
    public void testDeleteHandler_FeatureGroupDoesNotExists_Fails() {
        when(proxyClient.client().deleteFeatureGroup(any(DeleteFeatureGroupRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_FEATURE_GROUP_NAME));
    }

    @Test
    public void testDeleteHandler_VerifyStabilization_SuccessfulDelete() {
        final DescribeFeatureGroupResponse describeFeatureGroupResponse1 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.DELETING)
                        .build();

        final DeleteFeatureGroupResponse deleteFeatureGroupResponse = DeleteFeatureGroupResponse.builder()
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenReturn(describeFeatureGroupResponse1).thenThrow(ResourceNotFoundException.class);
        when(proxyClient.client().deleteFeatureGroup(any(DeleteFeatureGroupRequest.class)))
                .thenReturn(deleteFeatureGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
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
        final DescribeFeatureGroupResponse describeFeatureGroupResponse1 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.DELETING)
                        .build();

        final DescribeFeatureGroupResponse describeFeatureGroupResponse2 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.DELETE_FAILED)
                        .build();

        final DeleteFeatureGroupResponse deleteFeatureGroupResponse = DeleteFeatureGroupResponse.builder()
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenReturn(describeFeatureGroupResponse1).thenReturn(describeFeatureGroupResponse2);
        when(proxyClient.client().deleteFeatureGroup(any(DeleteFeatureGroupRequest.class)))
                .thenReturn(deleteFeatureGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandleRequest(request));
        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_FEATURE_GROUP_NAME));
    }


    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final DeleteHandler handler = new DeleteHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
