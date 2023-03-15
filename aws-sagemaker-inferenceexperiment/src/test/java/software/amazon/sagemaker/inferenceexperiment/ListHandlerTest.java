package software.amazon.sagemaker.inferenceexperiment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListInferenceExperimentsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListInferenceExperimentsResponse;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.awssdk.utils.DateUtils;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

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
        final ListInferenceExperimentsResponse listInferenceExperimentsResponse = getSdkListResponse();

        when(proxyClient.client().listInferenceExperiments(any(ListInferenceExperimentsRequest.class)))
                .thenReturn(listInferenceExperimentsResponse);

        final ResourceModel expectedResourceModel = getResponseResourceModel("Running", null);

        List<ResourceModel> expectedModels = new ArrayList<ResourceModel>();
        expectedModels.add(expectedResourceModel);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(expectedModels);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testListHandler_SimpleSuccess_NoResourceExist() {
        final ListInferenceExperimentsResponse listInferenceExperimentsResponse =
                ListInferenceExperimentsResponse.builder()
                        .inferenceExperiments(Collections.emptyList())
                        .nextToken(null)
                        .build();

        when(proxyClient.client().listInferenceExperiments(any(ListInferenceExperimentsRequest.class)))
                .thenReturn(listInferenceExperimentsResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(Collections.emptyList());
        assertThat(response.getNextToken()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testListHandler_ServiceInternalException() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("InternalError").build();
        AwsServiceException ex = SageMakerException.builder()
                .message(TEST_ERROR_MESSAGE)
                .awsErrorDetails(errorDetails)
                .build();

        when(proxyClient.client().listInferenceExperiments(any(ListInferenceExperimentsRequest.class)))
                .thenThrow(ex);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows( CfnServiceInternalErrorException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                "Failure reason: test error message (Service: null, Status Code: 0, Request ID: null)"));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ListHandler handler = new ListHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder().build();
    }

    private ResourceModel getResponseResourceModel(final String status, final String statusReason) {
        return ResourceModel.builder()
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .schedule(getCfnSchedule())
                .creationTime(DateUtils.formatIso8601Date(TEST_TIME))
                .lastModifiedTime(DateUtils.formatIso8601Date(TEST_TIME))
                .status(status)
                .statusReason(statusReason)
                .build();
    }
}