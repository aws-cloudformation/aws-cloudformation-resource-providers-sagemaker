package software.amazon.sagemaker.inferenceexperiment;

import software.amazon.awssdk.services.sagemaker.model.CaptureContentTypeHeader;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.EndpointMetadata;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentDataStorageConfig;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentSchedule;
import software.amazon.awssdk.services.sagemaker.model.ListInferenceExperimentsResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelInfrastructureConfig;
import software.amazon.awssdk.services.sagemaker.model.ModelVariantConfigSummary;
import software.amazon.awssdk.services.sagemaker.model.RealTimeInferenceConfig;
import software.amazon.awssdk.services.sagemaker.model.ShadowModeConfig;
import software.amazon.awssdk.services.sagemaker.model.ShadowModelVariantConfig;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.DateUtils;

import java.util.List;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {}

    /**
     * Translates resource object from sdk into a resource model
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeInferenceExperimentResponse awsResponse) {
        return ResourceModel.builder()
                .arn(awsResponse.arn())
                .name(awsResponse.name())
                .type(awsResponse.typeAsString())
                .roleArn(awsResponse.roleArn())
                .description(awsResponse.description())
                .creationTime(awsResponse.creationTime() == null ? null : DateUtils.formatIso8601Date(awsResponse.creationTime()))
                .lastModifiedTime(awsResponse.lastModifiedTime() == null ? null : DateUtils.formatIso8601Date(awsResponse.lastModifiedTime()))
                .status(awsResponse.statusAsString())
                .statusReason(awsResponse.statusReason())
                .desiredState(awsResponse.statusAsString())
                .endpointName(awsResponse.endpointMetadata().endpointName())
                .endpointMetadata(translate(awsResponse.endpointMetadata()))
                .schedule(translate(awsResponse.schedule()))
                .kmsKey(awsResponse.kmsKey())
                .dataStorageConfig(translate(awsResponse.dataStorageConfig()))
                .modelVariants(CollectionUtils.isNullOrEmpty(awsResponse.modelVariants()) ? null : awsResponse.modelVariants().stream()
                        .map(TranslatorForResponse::translate)
                        .collect(Collectors.toList()))
                .shadowModeConfig(translate(awsResponse.shadowModeConfig()))
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListInferenceExperimentsResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.inferenceExperiments())
                .map(summary -> ResourceModel.builder()
                        .name(summary.name())
                        .type(summary.typeAsString())
                        .roleArn(summary.roleArn())
                        .description(summary.description())
                        .schedule(translate(summary.schedule()))
                        .creationTime(summary.creationTime() == null ? null : DateUtils.formatIso8601Date(summary.creationTime()))
                        .lastModifiedTime(summary.lastModifiedTime() == null ? null : DateUtils.formatIso8601Date(summary.lastModifiedTime()))
                        .status(summary.statusAsString())
                        .statusReason(summary.statusReason())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Converts EndpointMetadata from a Sagemaker SDK object to a CFN resource model.
     * @param metadata Sagemaker EndpointMetadata object.
     * @return EndpointMetadata object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.EndpointMetadata translate(final EndpointMetadata metadata) {
        return metadata == null ? null : software.amazon.sagemaker.inferenceexperiment.EndpointMetadata.builder()
                .endpointName(metadata.endpointName())
                .endpointConfigName(metadata.endpointConfigName())
                .endpointStatus(metadata.endpointStatusAsString())
                .build();
    }

    /**
     * Converts InferenceExperimentSchedule from a Sagemaker SDK object to a CFN resource model.
     * @param schedule Sagemaker InferenceExperimentSchedule object.
     * @return InferenceExperimentSchedule object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.InferenceExperimentSchedule translate(final InferenceExperimentSchedule schedule) {
        return schedule == null ? null : software.amazon.sagemaker.inferenceexperiment.InferenceExperimentSchedule.builder()
                .startTime(schedule.startTime() == null ? null : DateUtils.formatIso8601Date(schedule.startTime()))
                .endTime(schedule.endTime() == null ? null : DateUtils.formatIso8601Date(schedule.endTime()))
                .build();
    }

    /**
     * Converts InferenceExperimentDataStorageConfig from a Sagemaker SDK object to a CFN resource model.
     * @param config Sagemaker InferenceExperimentDataStorageConfig object.
     * @return DataStorageConfig object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.DataStorageConfig translate(final InferenceExperimentDataStorageConfig config) {
        return config == null ? null : software.amazon.sagemaker.inferenceexperiment.DataStorageConfig.builder()
                .destination(config.destination())
                .kmsKey(config.kmsKey())
                .contentType(translate(config.contentType()))
                .build();
    }

    /**
     * Converts CaptureContentTypeHeader from a Sagemaker SDK object to a CFN resource model.
     * @param contentTypeHeader Sagemaker CaptureContentTypeHeader object.
     * @return CaptureContentTypeHeader object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.CaptureContentTypeHeader translate(final CaptureContentTypeHeader contentTypeHeader) {
        return contentTypeHeader == null ? null : software.amazon.sagemaker.inferenceexperiment.CaptureContentTypeHeader.builder()
                .csvContentTypes(contentTypeHeader.csvContentTypes())
                .jsonContentTypes(contentTypeHeader.jsonContentTypes())
                .build();
    }

    /**
     * Converts RealTimeInferenceConfig from a Sagemaker SDK object to a CFN resource model.
     * @param config Sagemaker RealTimeInferenceConfig object.
     * @return RealTimeInferenceConfig object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.RealTimeInferenceConfig translate(final RealTimeInferenceConfig config) {
        return config == null ? null : software.amazon.sagemaker.inferenceexperiment.RealTimeInferenceConfig.builder()
                .instanceType(config.instanceTypeAsString())
                .instanceCount(config.instanceCount())
                .build();
    }

    /**
     * Converts ModelInfrastructureConfig from a Sagemaker SDK object to a CFN resource model.
     * @param config Sagemaker ModelInfrastructureConfig object.
     * @return ModelInfrastructureConfig object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.ModelInfrastructureConfig translate(final ModelInfrastructureConfig config) {
        return config == null ? null : software.amazon.sagemaker.inferenceexperiment.ModelInfrastructureConfig.builder()
                .infrastructureType(config.infrastructureTypeAsString())
                .realTimeInferenceConfig(translate(config.realTimeInferenceConfig()))
                .build();
    }

    /**
     * Converts ModelVariantConfig from a Sagemaker SDK object to a CFN resource model.
     * @param config Sagemaker ModelVariantConfigSummary object.
     * @return ModelVariantConfig object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.ModelVariantConfig translate(final ModelVariantConfigSummary config) {
        return config == null ? null : software.amazon.sagemaker.inferenceexperiment.ModelVariantConfig.builder()
                .modelName(config.modelName())
                .variantName(config.variantName())
                .infrastructureConfig(translate(config.infrastructureConfig()))
                .build();
    }

    /**
     * Converts ShadowModelVariantConfig from a Sagemaker SDK object to a CFN resource model.
     * @param config Sagemaker ShadowModelVariantConfig object.
     * @return ShadowModelVariantConfig object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.ShadowModelVariantConfig translate(final ShadowModelVariantConfig config) {
        return config == null ? null : software.amazon.sagemaker.inferenceexperiment.ShadowModelVariantConfig.builder()
                .shadowModelVariantName(config.shadowModelVariantName())
                .samplingPercentage(config.samplingPercentage())
                .build();
    }

    /**
     * Converts ShadowModeConfig from a Sagemaker SDK object to a CFN resource model.
     * @param config Sagemaker ShadowModeConfig object.
     * @return ShadowModeConfig object from CFN resource provider.
     */
    static software.amazon.sagemaker.inferenceexperiment.ShadowModeConfig translate(final ShadowModeConfig config) {
        return config == null ? null : software.amazon.sagemaker.inferenceexperiment.ShadowModeConfig.builder()
                .sourceModelVariantName(config.sourceModelVariantName())
                .shadowModelVariants(config.shadowModelVariants().stream()
                        .map(TranslatorForResponse::translate)
                        .collect(Collectors.toList()))
                .build();
    }

    static software.amazon.sagemaker.inferenceexperiment.Tag translate(final Tag tag) {
        return tag == null ? null : software.amazon.sagemaker.inferenceexperiment.Tag.builder()
                .key(tag.key())
                .value(tag.value())
                .build();
    }

    static List<software.amazon.sagemaker.inferenceexperiment.Tag> translate(final List<Tag> tags) {
        return CollectionUtils.isNullOrEmpty(tags) ? null : tags.stream()
                .map(TranslatorForResponse::translate)
                .collect(Collectors.toList());
    }
}
