package software.amazon.sagemaker.featuregroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.FeatureGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.FeatureGroupSummary;
import software.amazon.awssdk.services.sagemaker.model.ListFeatureGroupsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListFeatureGroupsResponse;
import software.amazon.awssdk.services.sagemaker.model.OfflineStoreStatus;
import software.amazon.awssdk.services.sagemaker.model.OfflineStoreStatusValue;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    public static final String TEST_TOKEN = "testToken";

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
        final FeatureGroupSummary featureGroupSummary = FeatureGroupSummary.builder()
                .creationTime(TEST_TIME)
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .featureGroupArn(TEST_FEATURE_GROUP_ARN)
                .featureGroupStatus(FeatureGroupStatus.CREATED)
                .offlineStoreStatus(OfflineStoreStatus.builder()
                        .status(OfflineStoreStatusValue.ACTIVE)
                        .build())
                .build();

        final ListFeatureGroupsResponse listFeatureGroupsResponse =
                ListFeatureGroupsResponse.builder()
                        .featureGroupSummaries(Arrays.asList(featureGroupSummary))
                        .nextToken(TEST_TOKEN)
                        .build();

        when(proxyClient.client().listFeatureGroups(any(ListFeatureGroupsRequest.class)))
                .thenReturn(listFeatureGroupsResponse);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .build();

        List<ResourceModel> expectedModels = new ArrayList<>();
        expectedModels.add(expectedModelFromResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(expectedModels);
        assertThat(response.getNextToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testListHandler_SimpleSuccess_NoFeatureGroupExist() {
        final ListFeatureGroupsResponse listFeatureGroupsResponse =
                ListFeatureGroupsResponse.builder()
                        .featureGroupSummaries(Collections.emptyList())
                        .nextToken(null)
                        .build();

        when(proxyClient.client().listFeatureGroups(any(ListFeatureGroupsRequest.class)))
                .thenReturn(listFeatureGroupsResponse);

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

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ListHandler handler = new ListHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder().build();
    }
}