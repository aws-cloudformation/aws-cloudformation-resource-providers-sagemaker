package software.amazon.sagemaker.inferenceexperiment;

import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CaptureContentTypeHeader;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.EndpointMetadata;
import software.amazon.awssdk.services.sagemaker.model.EndpointStatus;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentDataStorageConfig;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentSchedule;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentSummary;
import software.amazon.awssdk.services.sagemaker.model.ListInferenceExperimentsResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelInfrastructureConfig;
import software.amazon.awssdk.services.sagemaker.model.ModelVariantConfigSummary;
import software.amazon.awssdk.services.sagemaker.model.RealTimeInferenceConfig;
import software.amazon.awssdk.services.sagemaker.model.ShadowModeConfig;
import software.amazon.awssdk.services.sagemaker.model.ShadowModelVariantConfig;
import software.amazon.awssdk.utils.DateUtils;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AbstractTestBase {
    protected static final String TEST_ENDPOINT_NAME = "testEndpointName";
    protected static final String TEST_ENDPOINT_CONFIG_NAME = "testEndpointConfigName";
    protected static final String TEST_ROLE_ARN = "sampleRoleArn";
    protected static final Instant TEST_TIME = Instant.now();
    protected static final Date TEST_SCHEDULE_START_TIME = Date.from(TEST_TIME);
    protected static final Date TEST_SCHEDULE_END_TIME = org.apache.commons.lang3.time.DateUtils.addDays(TEST_SCHEDULE_START_TIME, 7);
    protected static final String TEST_EXPERIMENT_ARN = "testInferenceExperimentArn";
    protected static final String TEST_EXPERIMENT_NAME = "testInferenceExperimentName";
    protected static final String TEST_EXPERIMENT_TYPE = "ShadowMode";
    protected static final String TEST_DESCRIPTION = "sampleDescription";
    protected static final String TEST_PROD_MODEL_NAME = "ProdModel";
    protected static final String TEST_PROD_VARIANT_NAME = "ProdVariant";
    protected static final String TEST_SHADOW_MODEL_NAME = "ShadowModel";
    protected static final String TEST_SHADOW_VARIANT_NAME = "ShadowVariant";
    protected static final String TEST_MODEL_INFRA_TYPE = "RealTimeInference";
    protected static final String TEST_INSTANCE_TYPE = "ml.m5.xlarge";
    protected static final String TEST_S3_BUCKET = "testS3bucket";
    protected static final String TEST_KMS_KEY = "testKmsKeyId";
    protected static final String TEST_JSON_CONTENT_TYPE = "application/json";
    protected static final String TEST_CSV_CONTENT_TYPE = "text/csv";
    protected static final String TEST_ERROR_MESSAGE = "test error message";
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }
    static ProxyClient<SageMakerClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final SageMakerClient sagemakerClient) {
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
                return sagemakerClient;
            }
        };
    }

    static DescribeInferenceExperimentResponse getSdkDescribeResponse(final String status, final String statusReason) {
        return DescribeInferenceExperimentResponse.builder()
                .arn(TEST_EXPERIMENT_ARN)
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .endpointMetadata(getSdkEndpointMetadata())
                .schedule(getSdkSchedule())
                .dataStorageConfig(getSdkDataStorageConfig())
                .modelVariants(getSdkModelVariants())
                .shadowModeConfig(getSdkShadowModeConfig())
                .creationTime(TEST_TIME)
                .lastModifiedTime(TEST_TIME)
                .status(status)
                .statusReason(statusReason)
                .build();
    }

    static ListInferenceExperimentsResponse getSdkListResponse() {
        return ListInferenceExperimentsResponse.builder()
                .inferenceExperiments(getSdkInferenceExperimentSummary())
                .build();
    }

    static InferenceExperimentSummary getSdkInferenceExperimentSummary() {
        return InferenceExperimentSummary.builder()
                .name(TEST_EXPERIMENT_NAME)
                .type(TEST_EXPERIMENT_TYPE)
                .roleArn(TEST_ROLE_ARN)
                .description(TEST_DESCRIPTION)
                .schedule(getSdkSchedule())
                .creationTime(TEST_TIME)
                .lastModifiedTime(TEST_TIME)
                .status("Running")
                .build();
    }

    static EndpointMetadata getSdkEndpointMetadata(){
        return EndpointMetadata.builder()
                .endpointName(TEST_ENDPOINT_NAME)
                .endpointConfigName(TEST_ENDPOINT_CONFIG_NAME)
                .endpointStatus(EndpointStatus.IN_SERVICE)
                .build();
    }

    static software.amazon.sagemaker.inferenceexperiment.EndpointMetadata getCfnEndpointMetadata() {
        return software.amazon.sagemaker.inferenceexperiment.EndpointMetadata.builder()
                .endpointName(TEST_ENDPOINT_NAME)
                .endpointConfigName(TEST_ENDPOINT_CONFIG_NAME)
                .endpointStatus(EndpointStatus.IN_SERVICE.toString())
                .build();
    }

    static InferenceExperimentSchedule getSdkSchedule() {
        return InferenceExperimentSchedule.builder()
                .startTime(TEST_SCHEDULE_START_TIME.toInstant())
                .endTime(TEST_SCHEDULE_END_TIME.toInstant())
                .build();
    }

    static software.amazon.sagemaker.inferenceexperiment.InferenceExperimentSchedule getCfnSchedule() {
        return software.amazon.sagemaker.inferenceexperiment.InferenceExperimentSchedule.builder()
                .startTime(DateUtils.formatIso8601Date(TEST_SCHEDULE_START_TIME.toInstant()))
                .endTime(DateUtils.formatIso8601Date(TEST_SCHEDULE_END_TIME.toInstant()))
                .build();
    }

    static InferenceExperimentDataStorageConfig getSdkDataStorageConfig() {
        return InferenceExperimentDataStorageConfig.builder()
                .destination(TEST_S3_BUCKET)
                .kmsKey(TEST_KMS_KEY)
                .contentType(CaptureContentTypeHeader.builder()
                        .csvContentTypes(TEST_CSV_CONTENT_TYPE)
                        .jsonContentTypes(TEST_JSON_CONTENT_TYPE)
                        .build())
                .build();
    }

    static DataStorageConfig getCfnDataStorageConfig() {
        return DataStorageConfig.builder()
                .destination(TEST_S3_BUCKET)
                .kmsKey(TEST_KMS_KEY)
                .contentType(software.amazon.sagemaker.inferenceexperiment.CaptureContentTypeHeader.builder()
                        .csvContentTypes(Collections.singletonList(TEST_CSV_CONTENT_TYPE))
                        .jsonContentTypes(Collections.singletonList(TEST_JSON_CONTENT_TYPE))
                        .build())
                .build();
    }

    static List<ModelVariantConfigSummary> getSdkModelVariants() {
        return ImmutableList.of(
                getSdkModelVariantConfig(TEST_PROD_MODEL_NAME, TEST_PROD_VARIANT_NAME),
                getSdkModelVariantConfig(TEST_SHADOW_MODEL_NAME, TEST_SHADOW_VARIANT_NAME)
        );
    }

    static List<ModelVariantConfig> getCfnModelVariants() {
        return ImmutableList.of(
                getCfnModelVariantConfig(TEST_PROD_MODEL_NAME, TEST_PROD_VARIANT_NAME),
                getCfnModelVariantConfig(TEST_SHADOW_MODEL_NAME, TEST_SHADOW_VARIANT_NAME)
        );
    }

    static ModelVariantConfigSummary getSdkModelVariantConfig(final String modelName, final String variantName) {
        return ModelVariantConfigSummary.builder()
                .modelName(modelName)
                .variantName(variantName)
                .infrastructureConfig(getSdkModelInfrastructureConfig())
                .build();
    }

    static ModelVariantConfig getCfnModelVariantConfig(final String modelName, final String variantName) {
        return ModelVariantConfig.builder()
                .modelName(modelName)
                .variantName(variantName)
                .infrastructureConfig(getCfnModelInfrastructureConfig())
                .build();
    }

    static ModelInfrastructureConfig getSdkModelInfrastructureConfig() {
        return ModelInfrastructureConfig.builder()
                .infrastructureType(TEST_MODEL_INFRA_TYPE)
                .realTimeInferenceConfig(getSdkRealTimeInferenceConfig())
                .build();
    }

    static software.amazon.sagemaker.inferenceexperiment.ModelInfrastructureConfig getCfnModelInfrastructureConfig() {
        return software.amazon.sagemaker.inferenceexperiment.ModelInfrastructureConfig.builder()
                .infrastructureType(TEST_MODEL_INFRA_TYPE)
                .realTimeInferenceConfig(getCfnRealTimeInferenceConfig())
                .build();
    }

    static RealTimeInferenceConfig getSdkRealTimeInferenceConfig() {
        return RealTimeInferenceConfig.builder()
                .instanceType(TEST_INSTANCE_TYPE)
                .instanceCount(1)
                .build();
    }

    static software.amazon.sagemaker.inferenceexperiment.RealTimeInferenceConfig getCfnRealTimeInferenceConfig() {
        return software.amazon.sagemaker.inferenceexperiment.RealTimeInferenceConfig.builder()
                .instanceType(TEST_INSTANCE_TYPE)
                .instanceCount(1)
                .build();
    }

    static ShadowModeConfig getSdkShadowModeConfig() {
        return ShadowModeConfig.builder()
                .sourceModelVariantName(TEST_PROD_VARIANT_NAME)
                .shadowModelVariants(Collections.singletonList(
                        ShadowModelVariantConfig.builder()
                                .shadowModelVariantName(TEST_SHADOW_VARIANT_NAME)
                                .samplingPercentage(100)
                                .build()))
                .build();
    }

    static software.amazon.sagemaker.inferenceexperiment.ShadowModeConfig getCfnShadowModeConfig() {
        return software.amazon.sagemaker.inferenceexperiment.ShadowModeConfig.builder()
                .sourceModelVariantName(TEST_PROD_VARIANT_NAME)
                .shadowModelVariants(Collections.singletonList(
                        software.amazon.sagemaker.inferenceexperiment.ShadowModelVariantConfig.builder()
                                .shadowModelVariantName(TEST_SHADOW_VARIANT_NAME)
                                .samplingPercentage(100)
                                .build()))
                .build();
    }
}