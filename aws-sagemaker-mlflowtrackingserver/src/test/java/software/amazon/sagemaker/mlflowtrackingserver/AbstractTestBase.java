package software.amazon.sagemaker.mlflowtrackingserver;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.TrackingServerStatus;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.Delay;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.delay.Constant;

public class AbstractTestBase {
    // Override normal handler backoff strategy to reduce unit testing delays
    protected static final Delay TEST_BACKOFF_STRATEGY = Constant.of().timeout(Duration.ofMinutes(60)).delay(Duration.ofSeconds(1)).build();
    protected static final String TEST_MLFLOW_VERSION = "2.12.2";
    protected static final String TEST_PATCH_DOWNGRADED_MLFLOW_VERSION = "2.12.1";
    protected static final String TEST_PATCH_UPGRADED_MLFLOW_VERSION = "2.12.3";
    protected static final String TEST_MINOR_DOWNGRADED_MLFLOW_VERSION = "2.11.0";
    protected static final String TEST_MINOR_UPGRADED_MLFLOW_VERSION = "2.13.0";
    protected static final String TEST_MAJOR_DOWNGRADED_MLFLOW_VERSION = "1.23.45";
    protected static final String TEST_MAJOR_UPGRADED_MLFLOW_VERSION = "3.45.67";
    protected static final String TEST_TRACKING_SERVER_ARN = "arn:aws:sagemaker:us-west-2:123456789012:mlflow-tracking-server/testTrackingServerName";
    protected static final String TEST_TRACKING_SERVER_NAME = "testTrackingServerName";
    protected static final String TEST_DEFAULT_ROLE_ARN = "testRoleArn";
    protected static final String TEST_UPDATED_ROLE_ARN = "testUpdatedRoleArn";
    protected static final String TEST_DEFAULT_ARTIFACT_STORE_URI = "s3://testArtifactStoreBucket/path";
    protected static final String TEST_UPDATED_ARTIFACT_STORE_URI = "s3://testUpdatedArtifactStoreBucket/path";
    protected static final boolean TEST_DEFAULT_AUTOMATIC_MODEL_REGISTRATION_STATUS = false;
    protected static final boolean TEST_UPDATED_AUTOMATIC_MODEL_REGISTRATION_STATUS = true;
    protected static final String TEST_TRACKING_SERVER_SIZE = "Small";
    protected static final String TEST_UPDATED_TRACKING_SERVER_SIZE = "Medium";
    protected static final String TEST_MAINTENANCE_WINDOW = "Mon:13:00";
    protected static final String TEST_UPDATED_MAINTENANCE_WINDOW = "Sun:12:00";
    protected static final Instant TEST_CREATION_TIME = Instant.now();
    protected static final Instant TEST_LAST_MODIFIED_TIME = TEST_CREATION_TIME;
    protected static final Map<String, String> TEST_CFN_TAGS = ImmutableMap.of("key1", "value1");
    protected static final Map<String, String> TEST_CFN_TAGS_2 = ImmutableMap.of("key2", "value2");
    protected static final List<Tag> TEST_SDK_TAGS = ImmutableList.of(Tag.builder().key("key1").value("value1").build());
    protected static final List<Tag> TEST_RESOURCE_TAGS = ImmutableList.of(Tag.builder().key("ResourceTagKey1").value("ResourceTagValue1").build());
    protected static final List<software.amazon.sagemaker.mlflowtrackingserver.Tag> TEST_CFN_MODEL_TAGS
            = ImmutableList.of(software.amazon.sagemaker.mlflowtrackingserver.Tag.builder()
            .key("key1").value("value1").build());
    protected static final List<software.amazon.sagemaker.mlflowtrackingserver.Tag> TEST_CFN_MODEL_ALL_TAGS = ImmutableList.of(
            software.amazon.sagemaker.mlflowtrackingserver.Tag.builder().key("key1").value("value1").build(),
            software.amazon.sagemaker.mlflowtrackingserver.Tag.builder().key("ResourceTagKey1").value("ResourceTagValue1").build()
    );
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    protected static ResourceModel createResourceModel(
            final boolean includeTags,
            boolean includeSystemResourceTags,
            final boolean useUpdatedProperties) {
        return createResourceModel(includeTags, includeSystemResourceTags, useUpdatedProperties, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
    }

    protected static ResourceModel createResourceModel(
            final boolean includeTags,
            boolean includeSystemResourceTags,
            final boolean useUpdatedProperties,
            final String mlflowVersion,
            final String roleArn) {
        ResourceModel.ResourceModelBuilder resourceModelBuilder = ResourceModel.builder()
                .artifactStoreUri(useUpdatedProperties ? TEST_UPDATED_ARTIFACT_STORE_URI : TEST_DEFAULT_ARTIFACT_STORE_URI)
                .automaticModelRegistration(useUpdatedProperties ? TEST_UPDATED_AUTOMATIC_MODEL_REGISTRATION_STATUS : TEST_DEFAULT_AUTOMATIC_MODEL_REGISTRATION_STATUS)
                .trackingServerName(TEST_TRACKING_SERVER_NAME)
                .mlflowVersion(mlflowVersion)
                .roleArn(roleArn)
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .trackingServerSize(useUpdatedProperties ? TEST_UPDATED_TRACKING_SERVER_SIZE : TEST_TRACKING_SERVER_SIZE)
                .weeklyMaintenanceWindowStart(useUpdatedProperties ? TEST_UPDATED_MAINTENANCE_WINDOW : TEST_MAINTENANCE_WINDOW);
        if (includeTags) {
            resourceModelBuilder.tags(includeSystemResourceTags ? TEST_CFN_MODEL_ALL_TAGS : TEST_CFN_MODEL_TAGS);
        }
        return resourceModelBuilder.build();
    }

    protected static DescribeMlflowTrackingServerResponse createDescribeMlflowTrackingServerResponse(
            TrackingServerStatus status,
            boolean useUpdatedProperties) {
        return createDescribeMlflowTrackingServerResponse(status, useUpdatedProperties, TEST_MLFLOW_VERSION);
    }

    protected static DescribeMlflowTrackingServerResponse createDescribeMlflowTrackingServerResponse(
            TrackingServerStatus status,
            boolean useUpdatedProperties,
            String mlflowVersion) {
        return DescribeMlflowTrackingServerResponse.builder()
                .artifactStoreUri(useUpdatedProperties ? TEST_UPDATED_ARTIFACT_STORE_URI : TEST_DEFAULT_ARTIFACT_STORE_URI)
                .automaticModelRegistration(useUpdatedProperties ? TEST_UPDATED_AUTOMATIC_MODEL_REGISTRATION_STATUS : TEST_DEFAULT_AUTOMATIC_MODEL_REGISTRATION_STATUS)
                .creationTime(TEST_CREATION_TIME)
                .lastModifiedTime(TEST_LAST_MODIFIED_TIME)
                .trackingServerName(TEST_TRACKING_SERVER_NAME)
                .mlflowVersion(mlflowVersion)
                .roleArn(TEST_DEFAULT_ROLE_ARN)
                .trackingServerArn(TEST_TRACKING_SERVER_ARN)
                .trackingServerSize(useUpdatedProperties ? TEST_UPDATED_TRACKING_SERVER_SIZE : TEST_TRACKING_SERVER_SIZE)
                .trackingServerStatus(status)
                .weeklyMaintenanceWindowStart(useUpdatedProperties ? TEST_UPDATED_MAINTENANCE_WINDOW : TEST_MAINTENANCE_WINDOW)
                .build();
    }

    static ProxyClient<SageMakerClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final SageMakerClient sageMakerClient) {
        return new ProxyClient<SageMakerClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public SageMakerClient client() {
                return sageMakerClient;
            }
        };
    }
}