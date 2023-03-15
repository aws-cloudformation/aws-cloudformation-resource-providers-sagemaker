package software.amazon.sagemaker.inferenceexperiment;

import com.amazonaws.util.CollectionUtils;
import com.google.common.collect.Maps;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.CaptureContentTypeHeader;
import software.amazon.awssdk.services.sagemaker.model.CreateInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentDataStorageConfig;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentSchedule;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentStatus;
import software.amazon.awssdk.services.sagemaker.model.ListInferenceExperimentsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ModelInfrastructureConfig;
import software.amazon.awssdk.services.sagemaker.model.ModelVariantAction;
import software.amazon.awssdk.services.sagemaker.model.ModelVariantConfig;
import software.amazon.awssdk.services.sagemaker.model.RealTimeInferenceConfig;
import software.amazon.awssdk.services.sagemaker.model.ShadowModeConfig;
import software.amazon.awssdk.services.sagemaker.model.ShadowModelVariantConfig;
import software.amazon.awssdk.services.sagemaker.model.StartInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateInferenceExperimentRequest;
import software.amazon.awssdk.utils.DateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for handlers like read/list
 */
final class TranslatorForRequest {

    private TranslatorForRequest() {}

    /**
     * Request to create a resource
     * @param model resource model
     * @return createInferenceExperimentRequest - service request to create a resource
     */
    static CreateInferenceExperimentRequest translateToCreateRequest(final ResourceModel model) {
        return CreateInferenceExperimentRequest.builder()
                .name(model.getName())
                .description(model.getDescription())
                .type(model.getType())
                .roleArn(model.getRoleArn())
                .endpointName(model.getEndpointName())
                .schedule(translate(model.getSchedule()))
                .kmsKey(model.getKmsKey())
                .dataStorageConfig(translate(model.getDataStorageConfig()))
                .modelVariants(CollectionUtils.isNullOrEmpty(model.getModelVariants()) ? null : model.getModelVariants().stream()
                        .map(TranslatorForRequest::translate)
                        .collect(Collectors.toList()))
                .shadowModeConfig(translate(model.getShadowModeConfig()))
                .tags(CollectionUtils.isNullOrEmpty(model.getTags()) ? null : model.getTags().stream()
                        .map(TranslatorForRequest::translate)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Request to read a resource
     * @param model resource model
     * @return describeInferenceExperimentRequest - the aws service request to describe a resource
     */
    static DescribeInferenceExperimentRequest translateToReadRequest(final ResourceModel model) {
        return DescribeInferenceExperimentRequest.builder()
                .name(model.getName())
                .build();
    }

    /**
     * Request to read a resource
     * @param name resource name
     * @return describeInferenceExperimentRequest - the aws service request to describe a resource
     */
    static DescribeInferenceExperimentRequest translateResourceNameToReadRequest(final String name) {
        return DescribeInferenceExperimentRequest.builder()
                .name(name)
                .build();
    }

    /**
     * Request to delete a resource
     * @param model resource model
     * @return deleteInferenceExperimentRequest the aws service request to delete a resource
     */
    static DeleteInferenceExperimentRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteInferenceExperimentRequest.builder()
                .name(model.getName())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     * @param model resource model
     * @return updateInferenceExperimentRequest the aws service request to modify a resource
     */
    static UpdateInferenceExperimentRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateInferenceExperimentRequest.builder()
                .name(model.getName())
                .description(model.getDescription())
                .schedule(translate(model.getSchedule()))
                .dataStorageConfig(translate(model.getDataStorageConfig()))
                .modelVariants(model.getModelVariants().stream()
                        .map(TranslatorForRequest::translate)
                        .collect(Collectors.toList()))
                .shadowModeConfig(translate(model.getShadowModeConfig()))
                .build();
    }

    /**
     * Request to update properties of a previously created resource with running state
     * @param model resource model
     * @return updateInferenceExperimentRequest the aws service request to modify a resource
     */
    static UpdateInferenceExperimentRequest translateToUpdateRunningResourceRequest(final ResourceModel model) {
        return UpdateInferenceExperimentRequest.builder()
                .name(model.getName())
                .description(model.getDescription())
                .schedule(translate(model.getSchedule()))
                .shadowModeConfig(translate(model.getShadowModeConfig()))
                .build();
    }

    /**
     * Request to start a previously created resource
     * @param model resource model
     * @return startInferenceExperimentRequest the aws service request to modify a resource
     */
    static StartInferenceExperimentRequest translateToStartRequest(final ResourceModel model) {
        return StartInferenceExperimentRequest.builder()
                .name(model.getName())
                .build();
    }

    /**
     * Request to stop a previously created resource
     * @param model resource model
     * @return stopInferenceExperimentRequest the aws service request to modify a resource
     */
    static StopInferenceExperimentRequest translateToStopRequest(final ResourceModel model) {
        return StopInferenceExperimentRequest.builder()
                .name(model.getName())
                .desiredState(model.getDesiredState())
                .desiredModelVariants(CollectionUtils.isNullOrEmpty(model.getModelVariants()) ? null : model.getModelVariants().stream()
                        .map(TranslatorForRequest::translate)
                        .collect(Collectors.toList()))
                .reason(model.getStatusReason())
                .build();
    }

    /**
     * Request to stop a previously created resource with model variant action overrides
     * @param request stop resource request
     * @param describeResponse describe resource response
     * @return stopInferenceExperimentRequest the aws service request to stop a resource
     */
    static StopInferenceExperimentRequest updateModelVariantActionToStopRequest(
            final StopInferenceExperimentRequest request,
            final DescribeInferenceExperimentResponse describeResponse) {

        final Map<String, ModelVariantConfig> modelVariantMap =
                Maps.uniqueIndex(request.desiredModelVariants(), ModelVariantConfig::variantName);
        final Map<String, ModelVariantAction> modelVariantActionMap = new HashMap<>();
        final ShadowModeConfig shadowModeConfig = describeResponse.shadowModeConfig();
        final String prodVariant = shadowModeConfig.sourceModelVariantName();
        boolean shouldPromoteShadow = false;
        if (modelVariantMap.containsKey(prodVariant)) {
            modelVariantActionMap.put(prodVariant, ModelVariantAction.RETAIN);
        } else {
            modelVariantActionMap.put(prodVariant, ModelVariantAction.REMOVE);
            shouldPromoteShadow = true;
        }
        for (ShadowModelVariantConfig variant : shadowModeConfig.shadowModelVariants()) {
            final String shadowVariant = variant.shadowModelVariantName();
            if (modelVariantMap.containsKey(shadowVariant)) {
                if (shouldPromoteShadow) {
                    modelVariantActionMap.put(shadowVariant, ModelVariantAction.PROMOTE);
                } else {
                    modelVariantActionMap.put(shadowVariant, ModelVariantAction.RETAIN);
                }
            } else {
                modelVariantActionMap.put(shadowVariant, ModelVariantAction.REMOVE);
            }
        }
        return StopInferenceExperimentRequest.builder()
                .name(request.name())
                .modelVariantActions(modelVariantActionMap)
                .desiredState(request.desiredState())
                .desiredModelVariants(request.desiredModelVariants())
                .reason(request.reason())
                .build();
    }

    /**
     * Request to stop a previously created resource with default model variant action
     * @param describeResponse describe resource response
     * @return stopInferenceExperimentRequest the aws service request to stop a resource
     */
    static StopInferenceExperimentRequest updateDefaultModelVariantActionToStopRequest(
            final DescribeInferenceExperimentResponse describeResponse) {
        final Map<String, ModelVariantAction> modelVariantActionMap = new HashMap<>();
        final ShadowModeConfig shadowModeConfig = describeResponse.shadowModeConfig();
        final String prodVariant = shadowModeConfig.sourceModelVariantName();
        modelVariantActionMap.put(prodVariant, ModelVariantAction.RETAIN);
        for (ShadowModelVariantConfig variant : shadowModeConfig.shadowModelVariants()) {
            final String shadowVariant = variant.shadowModelVariantName();
            modelVariantActionMap.put(shadowVariant, ModelVariantAction.RETAIN);
        }
        return StopInferenceExperimentRequest.builder()
                .name(describeResponse.name())
                .desiredState(InferenceExperimentStatus.COMPLETED.toString())
                .modelVariantActions(modelVariantActionMap)
                .build();
    }

    /**
     * Request to list properties of a previously created resource
     * @param nextToken token passed to the aws service describe resource request
     * @return awsRequest the aws service request to describe resources within aws account
     */
    static ListInferenceExperimentsRequest translateToListRequest(final String nextToken) {
        return ListInferenceExperimentsRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    /**
     * Request to add tags on the resource
     * @param tags list of Tag object to add on the resource
     * @param resourceArn resource ARN
     * @return awsRequest the aws service request to add tags
     */
    static AddTagsRequest translateToAddTagsRequest(final List<Tag> tags, final String resourceArn) {
        return AddTagsRequest.builder()
                .resourceArn(resourceArn)
                .tags(tags)
                .build();
    }

    /**
     * Request to delete tags on the resource
     * @param tags list of Tag object to delete on the resource
     * @param resourceArn resource ARN
     * @return awsRequest the aws service request to delete tags
     */
    static DeleteTagsRequest translateToDeleteTagsRequest(final List<Tag> tags, final String resourceArn) {
        return DeleteTagsRequest.builder()
                .resourceArn(resourceArn)
                .tagKeys(tags.stream().map(Tag::key).collect(Collectors.toList()))
                .build();
    }

    /**
     * Request to list tags on the resource
     * @param resourceArn resource ARN
     * @return awsRequest the aws service request to delete tags
     */
    static ListTagsRequest translateToListTagsRequest(final String resourceArn) {
        return ListTagsRequest.builder()
                .resourceArn(resourceArn)
                .build();
    }

    /**
     * Converts InferenceExperimentSchedule from a CFN resource model to a Sagemaker SDK object.
     * @param schedule schedule object from CFN resource provider.
     * @return Sagemaker InferenceExperimentSchedule object.
     */
    static InferenceExperimentSchedule translate(final software.amazon.sagemaker.inferenceexperiment.InferenceExperimentSchedule schedule) {
        return schedule == null ? null : InferenceExperimentSchedule.builder()
                .startTime(schedule.getStartTime() == null ? null : DateUtils.parseIso8601Date(schedule.getStartTime()))
                .endTime(schedule.getEndTime() == null? null : DateUtils.parseIso8601Date(schedule.getEndTime()))
                .build();
    }

    /**
     * Converts InferenceExperimentDataStorageConfig from a CFN resource model to a Sagemaker SDK object.
     * @param config config object from CFN resource provider.
     * @return Sagemaker InferenceExperimentDataStorageConfig object.
     */
    static InferenceExperimentDataStorageConfig translate(final software.amazon.sagemaker.inferenceexperiment.DataStorageConfig config) {
        return config == null ? null : InferenceExperimentDataStorageConfig.builder()
                .destination(config.getDestination())
                .kmsKey(config.getKmsKey())
                .contentType(translate(config.getContentType()))
                .build();
    }

    /**
     * Converts CaptureContentTypeHeader from a CFN resource model to a Sagemaker SDK object.
     * @param contentTypeHeader config object from CFN resource provider.
     * @return Sagemaker CaptureContentTypeHeader object.
     */
    static CaptureContentTypeHeader translate(final software.amazon.sagemaker.inferenceexperiment.CaptureContentTypeHeader contentTypeHeader) {
        return contentTypeHeader == null ? null : CaptureContentTypeHeader.builder()
                .csvContentTypes(contentTypeHeader.getCsvContentTypes())
                .jsonContentTypes(contentTypeHeader.getJsonContentTypes())
                .build();
    }

    /**
     * Converts RealTimeInferenceConfig from a CFN resource model to a Sagemaker SDK object.
     * @param config config object from CFN resource provider.
     * @return Sagemaker RealTimeInferenceConfig object.
     */
    static RealTimeInferenceConfig translate(final software.amazon.sagemaker.inferenceexperiment.RealTimeInferenceConfig config) {
        return config == null ? null : RealTimeInferenceConfig.builder()
                .instanceType(config.getInstanceType())
                .instanceCount(config.getInstanceCount())
                .build();
    }

    /**
     * Converts ModelInfrastructureConfig from a CFN resource model to a Sagemaker SDK object.
     * @param config config object from CFN resource provider.
     * @return Sagemaker ModelInfrastructureConfig object.
     */
    static ModelInfrastructureConfig translate(final software.amazon.sagemaker.inferenceexperiment.ModelInfrastructureConfig config) {
        return config == null ? null : ModelInfrastructureConfig.builder()
                .infrastructureType(config.getInfrastructureType())
                .realTimeInferenceConfig(translate(config.getRealTimeInferenceConfig()))
                .build();
    }

    /**
     * Converts ModelVariantConfig from a CFN resource model to a Sagemaker SDK object.
     * @param config config object from CFN resource provider.
     * @return Sagemaker ModelVariantConfig object.
     */
    static ModelVariantConfig translate(final software.amazon.sagemaker.inferenceexperiment.ModelVariantConfig config) {
        return config == null ? null : ModelVariantConfig.builder()
                .modelName(config.getModelName())
                .variantName(config.getVariantName())
                .infrastructureConfig(translate(config.getInfrastructureConfig()))
                .build();
    }

    /**
     * Converts ShadowModelVariantConfig from a CFN resource model to a Sagemaker SDK object.
     * @param config config object from CFN resource provider.
     * @return Sagemaker ShadowModelVariantConfig object.
     */
    static ShadowModelVariantConfig translate(final software.amazon.sagemaker.inferenceexperiment.ShadowModelVariantConfig config) {
        return config == null ? null : ShadowModelVariantConfig.builder()
                .shadowModelVariantName(config.getShadowModelVariantName())
                .samplingPercentage(config.getSamplingPercentage())
                .build();
    }

    /**
     * Converts ShadowModeConfig from a CFN resource model to a Sagemaker SDK object.
     * @param config config object from CFN resource provider.
     * @return Sagemaker ShadowModeConfig object.
     */
    static ShadowModeConfig translate(final software.amazon.sagemaker.inferenceexperiment.ShadowModeConfig config) {
        return config == null ? null : ShadowModeConfig.builder()
                .sourceModelVariantName(config.getSourceModelVariantName())
                .shadowModelVariants(config.getShadowModelVariants().stream()
                        .map(TranslatorForRequest::translate)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Converts Tag from a CFN resource model to a Sagemaker SDK object.
     * @param tag tag object from CFN resource provider.
     * @return Sagemaker Tag object.
     */
    static Tag translate(final software.amazon.sagemaker.inferenceexperiment.Tag tag) {
        return tag == null ? null : Tag.builder()
                .key(tag.getKey())
                .value(tag.getValue())
                .build();
    }
}