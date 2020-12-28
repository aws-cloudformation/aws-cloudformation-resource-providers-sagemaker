package software.amazon.sagemaker.featuregroup;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.FeatureDefinition;
import software.amazon.awssdk.services.sagemaker.model.FeatureGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.FeatureType;
import software.amazon.awssdk.services.sagemaker.model.OfflineStoreStatusValue;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
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
import java.util.List;

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
        List<FeatureDefinition> featureDefinitions =
                Arrays.asList(
                        software.amazon.awssdk.services.sagemaker.model.FeatureDefinition.builder()
                                .featureName("year").featureType(FeatureType.INTEGRAL.toString()).build(),
                        software.amazon.awssdk.services.sagemaker.model.FeatureDefinition.builder()
                                .featureName("name").featureType(FeatureType.STRING.toString()).build()
                );

        software.amazon.awssdk.services.sagemaker.model.OnlineStoreConfig onlineStoreConfig =
                software.amazon.awssdk.services.sagemaker.model.OnlineStoreConfig.builder()
                        .enableOnlineStore(true)
                        .securityConfig(
                                software.amazon.awssdk.services.sagemaker.model
                                        .OnlineStoreSecurityConfig.builder()
                                        .kmsKeyId("kms").build()
                        )
                        .build();

        software.amazon.awssdk.services.sagemaker.model.OfflineStoreConfig offlineStoreConfig =
                software.amazon.awssdk.services.sagemaker.model.OfflineStoreConfig.builder()
                        .dataCatalogConfig(software.amazon.awssdk.services.sagemaker.model.DataCatalogConfig.builder()
                                .catalog("c").database("d").tableName("t").build()
                        )
                        .s3StorageConfig(software.amazon.awssdk.services.sagemaker.model.S3StorageConfig.builder()
                                .s3Uri("s3").kmsKeyId("kms").build()
                        )
                        .disableGlueTableCreation(false)
                        .build();

        final DescribeFeatureGroupResponse describeFeatureGroupResponse =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureDefinitions(featureDefinitions)
                        .onlineStoreConfig(onlineStoreConfig)
                        .offlineStoreConfig(offlineStoreConfig)
                        .featureGroupStatus(FeatureGroupStatus.CREATED)
                        .failureReason("none")
                        .offlineStoreStatus(software.amazon.awssdk.services.sagemaker.model.OfflineStoreStatus.builder()
                                .blockedReason("no")
                                .status(OfflineStoreStatusValue.ACTIVE)
                                .build())
                        .build();


        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenReturn(describeFeatureGroupResponse);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                .description(TEST_DESCRIPTION)
                .roleArn(TEST_ROLE_ARN)
                .featureDefinitions(Arrays.asList(
                        software.amazon.sagemaker.featuregroup.FeatureDefinition.builder().featureName("year").featureType(FeatureType.INTEGRAL.toString()).build(),
                        software.amazon.sagemaker.featuregroup.FeatureDefinition.builder().featureName("name").featureType(FeatureType.STRING.toString()).build()
                ))
                .onlineStoreConfig(OnlineStoreConfig
                        .builder()
                        .enableOnlineStore(true)
                        .securityConfig(OnlineStoreSecurityConfig.builder().kmsKeyId("kms").build())
                        .build()
                )
                .offlineStoreConfig(OfflineStoreConfig.builder()
                        .dataCatalogConfig(DataCatalogConfig.builder().catalog("c").database("d").tableName("t").build())
                        .s3StorageConfig(S3StorageConfig.builder().s3Uri("s3").kmsKeyId("kms").build())
                        .disableGlueTableCreation(false).build())
                .build();

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
        verify(proxyClient.client()).describeFeatureGroup(any(DescribeFeatureGroupRequest.class));
    }

    @Test
    public void testReadHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .message("test error message")
                .statusCode(500)
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.READ));
    }

    @Test
    public void testReadHandler_ResourceNotFoundException() {
        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenThrow(ResourceNotFoundException.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getRequestResourceModel())
                .build();
        Exception exception = assertThrows(CfnNotFoundException.class, () -> invokeHandleRequest(request));
        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_FEATURE_GROUP_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final ReadHandler handler = new ReadHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }

    private ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .build();
    }
}