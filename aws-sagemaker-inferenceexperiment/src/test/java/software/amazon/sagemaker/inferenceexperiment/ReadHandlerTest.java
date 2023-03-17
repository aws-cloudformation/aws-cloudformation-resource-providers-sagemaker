package software.amazon.sagemaker.inferenceexperiment;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.utils.DateUtils;
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
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

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
        final DescribeInferenceExperimentResponse describeInferenceExperimentResponse =
                getSdkDescribeResponse("Running", null);
        final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                .tags(Tag.builder()
                        .key("tag-key")
                        .value("tag-value")
                        .build())
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenReturn(describeInferenceExperimentResponse);
        when(proxyClient.client().listTags(any(ListTagsRequest.class)))
                .thenReturn(listTagsResponse);

        ResourceModel expectedModelFromResponse = getResponseResourceModel(describeInferenceExperimentResponse.statusAsString(), null);
        expectedModelFromResponse.setTags(Arrays.asList(software.amazon.sagemaker.inferenceexperiment.Tag.builder()
                .key("tag-key")
                .value("tag-value")
                .build()
        ));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testReadHandler_ClientErrorException() {
        final AwsServiceException resourceNotFoundException = AwsServiceException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenThrow(resourceNotFoundException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                "Failure reason: test error message"));
    }

    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeInferenceExperiment(any(DescribeInferenceExperimentRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_EXPERIMENT_NAME));
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .name(TEST_EXPERIMENT_NAME)
                .build();
    }

    private ResourceModel getResponseResourceModel(final String status, final String statusReason) {
        return ResourceModel.builder()
                .arn(TEST_EXPERIMENT_ARN)
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .endpointName(TEST_ENDPOINT_NAME)
                .endpointMetadata(getCfnEndpointMetadata())
                .schedule(getCfnSchedule())
                .dataStorageConfig(getCfnDataStorageConfig())
                .modelVariants(getCfnModelVariants())
                .shadowModeConfig(getCfnShadowModeConfig())
                .endpointMetadata(getCfnEndpointMetadata())
                .creationTime(DateUtils.formatIso8601Date(TEST_TIME))
                .lastModifiedTime(DateUtils.formatIso8601Date(TEST_TIME))
                .status(status)
                .statusReason(statusReason)
                .desiredState(status)
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}