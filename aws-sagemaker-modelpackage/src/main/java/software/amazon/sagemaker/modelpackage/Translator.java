package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackagesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackagesResponse;
import software.amazon.awssdk.services.sagemaker.model.UpdateModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.AdditionalInferenceSpecificationDefinition;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageContainerDefinition;
import software.amazon.awssdk.services.sagemaker.model.ProductionVariantInstanceType;
import software.amazon.awssdk.services.sagemaker.model.TransformInstanceType;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.Instant;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  private static List<ProductionVariantInstanceType> validateInferenceInstanceType(List<String> instanceList) {
    return instanceList.stream()
        .map(e -> ProductionVariantInstanceType.fromValue(e))
        .collect(Collectors.toList());
  }

  private static List<TransformInstanceType> validateTransformInstanceType(List<String> instanceList) {
    return instanceList.stream()
        .map(e -> TransformInstanceType.fromValue(e))
        .collect(Collectors.toList());
  }

  /**
   * Validation for CreateModelPackage ModelPackageName input
   * Not adding length, pattern constraints since the same model name will be reference in CreateMP as name
   * Input and in Describe/DeleteMP as arn/name input
   * @param  modelPackageName model package name
   */
  static void validateModelPackageName(final String modelPackageName) {
    if (!modelPackageName.matches("^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}$")) {
      throw new CfnInvalidRequestException("Model Package Name should follow pattern ^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}$");
    }
    if (modelPackageName.length()<1 && modelPackageName.length()>63) {
      throw new CfnInvalidRequestException("Model Package Name should have Min Length 1 and Lax Length 63.");
    }
  }

  /**
   * Request to create a modelpackage
   * @param model resource model
   * @return awsRequest the aws service request to create a modelpackage
   */
  static CreateModelPackageRequest translateToCreateRequest(final ResourceModel model) {
    // Adding additional validation for MP Name input since the
    // same ResourceModel Name is used to represent ARN in case of other APIs
    if (model.getModelPackageName() != null) {
      validateModelPackageName(model.getModelPackageName());
    }
    List<Tag> tags = cfnTagsToSdkTags(model.getTags());
    CreateModelPackageRequest request;
    request = CreateModelPackageRequest.builder()
          .additionalInferenceSpecifications(cfnAdditionalInferenceSpecificationToSdk(model.getAdditionalInferenceSpecifications()))
          .certifyForMarketplace(model.getCertifyForMarketplace())
          .clientToken(model.getClientToken())
          .inferenceSpecification(translate(model.getInferenceSpecification()))
          .metadataProperties(translate(model.getMetadataProperties()))
          .modelApprovalStatus(model.getModelApprovalStatus())
          .modelMetrics(translate(model.getModelMetrics()))
          .modelPackageDescription(model.getModelPackageDescription())
          .modelPackageGroupName(model.getModelPackageGroupName())
          .modelPackageName(model.getModelPackageName())
          .sourceAlgorithmSpecification(translate(model.getSourceAlgorithmSpecification()))
          .tags(tags)
          .validationSpecification(translate(model.getValidationSpecification()))
          .customerMetadataProperties(translateMapOfObjectsToMapOfStrings(model.getCustomerMetadataProperties()))
          .domain(model.getDomain())
          .samplePayloadUrl(model.getSamplePayloadUrl())
          .task(model.getTask())
          .driftCheckBaselines(translate(model.getDriftCheckBaselines()))
          .build();

    return request;
  }

  /**
   * Translates tag objects from resource model into list of tag objects of sdk
   * @param tags, resource model tags
   * @return list of sdk tags
   */
  static List<Tag> cfnTagsToSdkTags(final List<software.amazon.sagemaker.modelpackage.Tag> tags) {
    if (tags == null) {
      return new ArrayList<>();
    }
    for (final software.amazon.sagemaker.modelpackage.Tag tag : tags) {
      if (tag.getKey() == null) {
        throw new CfnInvalidRequestException("Tags cannot have a null key");
      }
      if (tag.getValue() == null) {
        throw new CfnInvalidRequestException("Tags cannot have a null value");
      }
    }
    return tags.stream()
        .map(e -> Tag.builder()
            .key(e.getKey())
            .value(e.getValue())
            .build())
        .collect(Collectors.toList());
  }

  /**
   * Translates tag objects from resource model into list of tag objects of sdk
   * @param additionalInferenceSpecificationDefinitions, resource model additionalInferenceSpecifications
   * @return list of sdk additionalInferenceSpecifications
   */
  static List<AdditionalInferenceSpecificationDefinition> cfnAdditionalInferenceSpecificationToSdk(final List<software.amazon.sagemaker.modelpackage.AdditionalInferenceSpecificationDefinition> additionalInferenceSpecificationDefinitions) {
    if (additionalInferenceSpecificationDefinitions == null || isEmpty(additionalInferenceSpecificationDefinitions)) {
      return null;
    }
    return additionalInferenceSpecificationDefinitions.stream()
        .map(e -> software.amazon.awssdk.services.sagemaker.model.AdditionalInferenceSpecificationDefinition.builder()
            .containers(cfnContainersToSdk(e.getContainers()))
            .description(e.getDescription())
            .name(e.getName())
            .supportedContentTypes(e.getSupportedContentTypes())
            .supportedRealtimeInferenceInstanceTypes(validateInferenceInstanceType(e.getSupportedRealtimeInferenceInstanceTypes()))
            .supportedResponseMIMETypes(e.getSupportedResponseMIMETypes())
            .supportedTransformInstanceTypes(validateTransformInstanceType(e.getSupportedTransformInstanceTypes()))
            .build())
        .collect(Collectors.toList());
  }

  static List<ModelPackageContainerDefinition> cfnContainersToSdk(final List<software.amazon.sagemaker.modelpackage.ModelPackageContainerDefinition> containers) {
    if (containers == null || isEmpty(containers)) {
      return null;
    }
    return containers.stream()
        .map(e -> {ModelPackageContainerDefinition cont = ModelPackageContainerDefinition.builder()
            .containerHostname(e.getContainerHostname())
            .framework(e.getFramework())
            .frameworkVersion(e.getFrameworkVersion())
            .nearestModelName(e.getNearestModelName())
            .modelInput(translate(e.getModelInput()))
            .environment(translateMapOfObjectsToMapOfStrings(e.getEnvironment()))
            .image(e.getImage())
            .imageDigest(e.getImageDigest())
            .modelDataUrl(e.getModelDataUrl())
            .productId(e.getProductId())
            .build();
          return cont;
        })
        .collect(Collectors.toList());
  }

  private static software.amazon.awssdk.services.sagemaker.model.ModelInput translate(
      ModelInput modelInput) {
    return modelInput == null? null : software.amazon.awssdk.services.sagemaker.model.ModelInput.builder()
        .dataInputConfig(modelInput.getDataInputConfig())
        .build();
  }

  static Map<String, String> translateMapOfObjectsToMapOfStrings(final Map<String, Object> mapOfObjects) {
    return (mapOfObjects == null || isEmpty(mapOfObjects)) ? null : mapOfObjects.entrySet().stream().collect(
        Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue())
    );
  }

  private static List<software.amazon.awssdk.services.sagemaker.model.SourceAlgorithm> translateSourceAlgorithms(
      List<SourceAlgorithm> sourceAlgorithm) {
    if (sourceAlgorithm == null || isEmpty(sourceAlgorithm)) {
      return null;
    }
    return sourceAlgorithm.stream()
        .map(e -> software.amazon.awssdk.services.sagemaker.model.SourceAlgorithm.builder()
            .algorithmName(e.getAlgorithmName())
            .modelDataUrl(e.getModelDataUrl())
            .build())
        .collect(Collectors.toList());
  }

  private static software.amazon.awssdk.services.sagemaker.model.InferenceSpecification translate(
      InferenceSpecification inferenceSpecification) {
    return inferenceSpecification == null? null : software.amazon.awssdk.services.sagemaker.model.InferenceSpecification.builder()
        .containers(cfnContainersToSdk(inferenceSpecification.getContainers()))
        .supportedContentTypes(inferenceSpecification.getSupportedContentTypes())
        .supportedRealtimeInferenceInstanceTypes(validateInferenceInstanceType(inferenceSpecification.getSupportedRealtimeInferenceInstanceTypes()))
        .supportedResponseMIMETypes(inferenceSpecification.getSupportedResponseMIMETypes())
        .supportedTransformInstanceTypes(validateTransformInstanceType(inferenceSpecification.getSupportedTransformInstanceTypes()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.MetadataProperties translate(
      MetadataProperties metadataProperties) {
    return metadataProperties == null? null : software.amazon.awssdk.services.sagemaker.model.MetadataProperties.builder()
        .commitId(metadataProperties.getCommitId())
        .generatedBy(metadataProperties.getGeneratedBy())
        .projectId(metadataProperties.getProjectId())
        .repository(metadataProperties.getRepository())
        .build();
  }


  private static software.amazon.awssdk.services.sagemaker.model.MetricsSource translate(
      MetricsSource metricsSource) {
    return metricsSource == null? null : software.amazon.awssdk.services.sagemaker.model.MetricsSource.builder()
        .contentDigest(metricsSource.getContentDigest())
        .contentType(metricsSource.getContentType())
        .s3Uri(metricsSource.getS3Uri())
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.FileSource translate(
      FileSource fileSource) {
    return fileSource == null? null : software.amazon.awssdk.services.sagemaker.model.FileSource.builder()
        .contentDigest(fileSource.getContentDigest())
        .contentType(fileSource.getContentType())
        .s3Uri(fileSource.getS3Uri())
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.ModelMetrics translate(
      ModelMetrics modelMetrics) {
    return modelMetrics == null? null : software.amazon.awssdk.services.sagemaker.model.ModelMetrics.builder()
        .bias(translate(modelMetrics.getBias()))
        .explainability(translate(modelMetrics.getExplainability()))
        .modelDataQuality(translate(modelMetrics.getModelDataQuality()))
        .modelQuality(translate(modelMetrics.getModelQuality()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.Bias translate(
      Bias bias) {

    return bias == null? null : software.amazon.awssdk.services.sagemaker.model.Bias.builder()
        .report(translate(bias.getReport()))
        .postTrainingReport(translate(bias.getPostTrainingReport()))
        .preTrainingReport(translate(bias.getPreTrainingReport()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.Explainability translate(
      Explainability explainability) {
    return explainability == null? null : software.amazon.awssdk.services.sagemaker.model.Explainability.builder()
        .report(translate(explainability.getReport()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.ModelDataQuality translate(
      ModelDataQuality modelDataQuality) {
    return modelDataQuality == null? null : software.amazon.awssdk.services.sagemaker.model.ModelDataQuality.builder()
        .constraints(translate(modelDataQuality.getConstraints()))
        .statistics(translate(modelDataQuality.getStatistics()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.ModelQuality translate(
      ModelQuality modelQuality) {
    return modelQuality == null? null : software.amazon.awssdk.services.sagemaker.model.ModelQuality.builder()
        .constraints(translate(modelQuality.getConstraints()))
        .statistics(translate(modelQuality.getStatistics()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.DriftCheckBaselines translate(
      DriftCheckBaselines driftCheckBaselines) {
    return driftCheckBaselines == null? null : software.amazon.awssdk.services.sagemaker.model.DriftCheckBaselines.builder()
        .bias(translate(driftCheckBaselines.getBias()))
        .explainability(translate(driftCheckBaselines.getExplainability()))
        .modelDataQuality(translate(driftCheckBaselines.getModelDataQuality()))
        .modelQuality(translate(driftCheckBaselines.getModelQuality()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.DriftCheckBias translate(
      DriftCheckBias bias) {

    return bias == null? null : software.amazon.awssdk.services.sagemaker.model.DriftCheckBias.builder()
        .configFile(translate(bias.getConfigFile()))
        .postTrainingConstraints(translate(bias.getPostTrainingConstraints()))
        .preTrainingConstraints(translate(bias.getPreTrainingConstraints()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.DriftCheckExplainability translate(
      DriftCheckExplainability explainability) {
    return explainability == null? null : software.amazon.awssdk.services.sagemaker.model.DriftCheckExplainability.builder()
        .configFile(translate(explainability.getConfigFile()))
        .constraints(translate(explainability.getConstraints()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.DriftCheckModelDataQuality translate(
      DriftCheckModelDataQuality modelDataQuality) {
    return modelDataQuality == null? null : software.amazon.awssdk.services.sagemaker.model.DriftCheckModelDataQuality.builder()
        .constraints(translate(modelDataQuality.getConstraints()))
        .statistics(translate(modelDataQuality.getStatistics()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.DriftCheckModelQuality translate(
      DriftCheckModelQuality modelQuality) {
    return modelQuality == null? null : software.amazon.awssdk.services.sagemaker.model.DriftCheckModelQuality.builder()
        .constraints(translate(modelQuality.getConstraints()))
        .statistics(translate(modelQuality.getStatistics()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.SourceAlgorithmSpecification translate(
      SourceAlgorithmSpecification sourceAlgorithmSpecification) {
    return sourceAlgorithmSpecification == null? null : software.amazon.awssdk.services.sagemaker.model.SourceAlgorithmSpecification.builder()
        .sourceAlgorithms(translateSourceAlgorithms(sourceAlgorithmSpecification.getSourceAlgorithms()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.ModelPackageValidationSpecification translate(
      ValidationSpecification validationSpecification) {
    return validationSpecification == null? null : software.amazon.awssdk.services.sagemaker.model.ModelPackageValidationSpecification.builder()
        .validationProfiles(translateValidationProfile(validationSpecification.getValidationProfiles()))
        .validationRole(validationSpecification.getValidationRole())
        .build();
  }

  private static List<software.amazon.awssdk.services.sagemaker.model.ModelPackageValidationProfile> translateValidationProfile(
      List<ValidationProfile> validationProfiles) {
    if (validationProfiles == null) {
      return null;
    }
    return validationProfiles.stream()
        .map(e -> software.amazon.awssdk.services.sagemaker.model.ModelPackageValidationProfile.builder()
            .profileName(e.getProfileName())
            .transformJobDefinition(translate(e.getTransformJobDefinition()))
            .build())
        .collect(Collectors.toList());
  }

  private static software.amazon.awssdk.services.sagemaker.model.TransformJobDefinition translate(
      TransformJobDefinition transformJobDefinition) {
    return transformJobDefinition == null? null : software.amazon.awssdk.services.sagemaker.model.TransformJobDefinition.builder()
        .batchStrategy(transformJobDefinition.getBatchStrategy())
        .environment(translateMapOfObjectsToMapOfStrings(transformJobDefinition.getEnvironment()))
        .maxConcurrentTransforms(transformJobDefinition.getMaxConcurrentTransforms())
        .maxPayloadInMB(transformJobDefinition.getMaxPayloadInMB())
        .transformInput(translate(transformJobDefinition.getTransformInput()))
        .transformOutput(translate(transformJobDefinition.getTransformOutput()))
        .transformResources(translate(transformJobDefinition.getTransformResources()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.TransformInput translate(
      TransformInput transformInput) {
    return transformInput == null? null : software.amazon.awssdk.services.sagemaker.model.TransformInput.builder()
        .splitType(transformInput.getSplitType())
        .compressionType(transformInput.getCompressionType())
        .contentType(transformInput.getContentType())
        .dataSource(translate(transformInput.getDataSource()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.TransformDataSource translate(
      DataSource dataSource) {
    return dataSource == null? null : software.amazon.awssdk.services.sagemaker.model.TransformDataSource.builder()
        .s3DataSource(translate(dataSource.getS3DataSource()))
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.TransformS3DataSource translate(
      S3DataSource s3DataSource) {
    return s3DataSource == null? null : software.amazon.awssdk.services.sagemaker.model.TransformS3DataSource.builder()
        .s3DataType(s3DataSource.getS3DataType())
        .s3Uri(s3DataSource.getS3Uri())
        .build();
  }


  private static software.amazon.awssdk.services.sagemaker.model.TransformResources translate(
      TransformResources transformResources) {
    return transformResources == null? null : software.amazon.awssdk.services.sagemaker.model.TransformResources.builder()
        .instanceCount(transformResources.getInstanceCount())
        .instanceType(transformResources.getInstanceType())
        .volumeKmsKeyId(transformResources.getVolumeKmsKeyId())
        .build();
  }

  private static software.amazon.awssdk.services.sagemaker.model.TransformOutput translate(
      TransformOutput transformOutput) {
    return transformOutput == null? null : software.amazon.awssdk.services.sagemaker.model.TransformOutput.builder()
        .accept(transformOutput.getAccept())
        .assembleWith(transformOutput.getAssembleWith())
        .kmsKeyId(transformOutput.getKmsKeyId())
        .s3OutputPath(transformOutput.getS3OutputPath())
        .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static DescribeModelPackageRequest translateToReadRequest(final ResourceModel model) {
    if (model.getModelPackageName() != null)
        return DescribeModelPackageRequest.builder()
            .modelPackageName(model.getModelPackageName())
            .build();
    // covers versioned describeMP while creating
    return DescribeModelPackageRequest.builder()
        .modelPackageName(model.getModelPackageArn())
        .build();

  }

  /**
   * Translates the model to request to list tags of model package group
   * @param model resource model
   * @return awsRequest the aws service to list tags of model package group
   */
  static ListTagsRequest translateToListTagsRequest(final ResourceModel model) {
    if (model.getModelPackageGroupName() != null) {
      throw new CfnInvalidRequestException("Tag operations are supported only on ModelPackage Group not on ModelPackage Versions.");
    }
    return ListTagsRequest.builder()
        .resourceArn(model.getModelPackageArn())
        .build();
  }

  /**
   * Translates time instance to string
   * @param time the instant time
   * @return string time
   */

  static String convertTime(Instant time) {
    if (time == null) {
      return null;
    }
    return time.toString();
  }

  static ResourceModel translateFromReadResponse(final DescribeModelPackageResponse awsResponse) {
    // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L58-L73
    return ResourceModel.builder()
        .approvalDescription(awsResponse.approvalDescription())
        .certifyForMarketplace(awsResponse.certifyForMarketplace())
        .createdBy(translateFromResponse(awsResponse.createdBy()))
        .creationTime(awsResponse.creationTime().toString())
        .customerMetadataProperties(translateMapOfStringsMapOfObjects(awsResponse.customerMetadataProperties()))
        .inferenceSpecification(translateFromResponse(awsResponse.inferenceSpecification()))
        .lastModifiedBy(translateFromResponse(awsResponse.lastModifiedBy()))
        .lastModifiedTime(convertTime(awsResponse.lastModifiedTime()))
        .metadataProperties(translateFromResponse(awsResponse.metadataProperties()))
        .modelApprovalStatus(awsResponse.modelApprovalStatusAsString())
        .modelMetrics(translateFromResponse(awsResponse.modelMetrics()))
        .modelPackageArn(awsResponse.modelPackageArn())
        .modelPackageDescription(awsResponse.modelPackageDescription())
        .modelPackageGroupName(awsResponse.modelPackageGroupName())
        .modelPackageName(awsResponse.modelPackageName())
        .modelPackageStatus(awsResponse.modelPackageStatus().toString())
        .modelPackageStatusDetails(translateFromResponse(awsResponse.modelPackageStatusDetails()))
        .modelPackageVersion(awsResponse.modelPackageVersion())
        .sourceAlgorithmSpecification(translateFromResponse(awsResponse.sourceAlgorithmSpecification()))
        .validationSpecification(translateFromResponse(awsResponse.validationSpecification()))
        .task(awsResponse.task())
        .samplePayloadUrl(awsResponse.samplePayloadUrl())
        .driftCheckBaselines(translateFromResponse(awsResponse.driftCheckBaselines()))
        .domain(awsResponse.domain())
        .additionalInferenceSpecifications(sdkAdditionalInferenceSpecificationsToCfn(awsResponse.additionalInferenceSpecifications()))
        .build();
  }

  /**
   * Translates tag objects from sdk into list of tag objects in resource model
   * @param tags, sdk tags got from the aws service response
   * @return list of resource model tags
   */
  static List<software.amazon.sagemaker.modelpackage.Tag> sdkTagsToCfnTags(final List<Tag> tags) {
    if (tags == null || tags.isEmpty()) {
      return null;
    }
    final List<software.amazon.sagemaker.modelpackage.Tag> cfnTags =
        tags.stream()
            .map(e -> software.amazon.sagemaker.modelpackage.Tag.builder()
                .key(e.key())
                .value(e.value())
                .build())
            .collect(Collectors.toList());
    return cfnTags;
  }

  static List<software.amazon.sagemaker.modelpackage.AdditionalInferenceSpecificationDefinition> sdkAdditionalInferenceSpecificationsToCfn(final List<AdditionalInferenceSpecificationDefinition> additionalInferenceSpecificationDefinitions) {
    if (additionalInferenceSpecificationDefinitions == null || additionalInferenceSpecificationDefinitions.isEmpty()) {
      return null;
    }
    return additionalInferenceSpecificationDefinitions.stream()
        .map(e -> software.amazon.sagemaker.modelpackage.AdditionalInferenceSpecificationDefinition.builder()
            .containers(sdkContainersToCfn(e.containers()))
            .description(e.description())
            .name(e.name())
            .supportedContentTypes(e.supportedContentTypes())
            .supportedRealtimeInferenceInstanceTypes(translateFromResponseSupportedRealtimeInferenceInstanceTypes(e.supportedRealtimeInferenceInstanceTypes()))
            .supportedResponseMIMETypes(e.supportedResponseMIMETypes())
            .supportedTransformInstanceTypes(translateFromResponseSupportedTransformInstanceTypes(e.supportedTransformInstanceTypes()))
            .build())
        .collect(Collectors.toList());
  }

  static List<software.amazon.sagemaker.modelpackage.ModelPackageContainerDefinition> sdkContainersToCfn(final List<ModelPackageContainerDefinition> containers) {
    if (containers == null || containers.isEmpty()) {
      return null;
    }
    return containers.stream()
        .map(e -> software.amazon.sagemaker.modelpackage.ModelPackageContainerDefinition.builder()
            .containerHostname(e.containerHostname())
            .modelInput(translateFromResponseModelInput(e.modelInput()))
            .nearestModelName(e.nearestModelName())
            .framework(e.framework())
            .frameworkVersion(e.frameworkVersion())
            .environment(translateMapOfStringsMapOfObjects(e.environment()))
            .image(e.image())
            .imageDigest(e.imageDigest())
            .modelDataUrl(e.modelDataUrl())
            .productId(e.productId())
            .build())
        .collect(Collectors.toList());
  }

  private static ModelInput translateFromResponseModelInput(
      software.amazon.awssdk.services.sagemaker.model.ModelInput modelInput) {
    return modelInput == null? null : ModelInput.builder()
        .dataInputConfig(modelInput.dataInputConfig())
        .build();
  }

  static Map<String, Object> translateMapOfStringsMapOfObjects(final Map<String, String> mapOfStrings) {
    return (mapOfStrings == null || isEmpty(mapOfStrings)) ? null : mapOfStrings.entrySet().stream().collect(
        Collectors.toMap(Map.Entry::getKey, e -> (Object)e.getValue())
    );
  }


  private static UserContext translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.UserContext userContext) {
    return userContext == null? null : UserContext.builder()
        .domainId(userContext.domainId())
        .userProfileArn(userContext.userProfileArn())
        .userProfileName(userContext.userProfileName())
        .build();
  }

  private static ModelPackageStatusDetails translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.ModelPackageStatusDetails modelPackageStatusDetails) {
    return modelPackageStatusDetails == null? null : ModelPackageStatusDetails.builder()
        .imageScanStatuses(translateFromResponse(modelPackageStatusDetails.imageScanStatuses()))
        .validationStatuses(translateFromResponse(modelPackageStatusDetails.validationStatuses()))
        .build();
  }

  private static List<ModelPackageStatusItem> translateFromResponse(
      List<software.amazon.awssdk.services.sagemaker.model.ModelPackageStatusItem> modelPackageStatusItems) {
    return modelPackageStatusItems != null && isNotEmpty(modelPackageStatusItems) ?
        modelPackageStatusItems.stream()
            .map(e -> ModelPackageStatusItem.builder()
                .failureReason(e.failureReason())
                .name(e.name())
                .status(e.status().toString())
                .build())
            .collect(Collectors.toList()): null;
  }

  private static DriftCheckBaselines translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.DriftCheckBaselines driftCheckBaselines) {
    return driftCheckBaselines == null? null : DriftCheckBaselines.builder()
        .bias(translateFromResponse(driftCheckBaselines.bias()))
        .explainability(translateFromResponse(driftCheckBaselines.explainability()))
        .modelDataQuality(translateFromResponse(driftCheckBaselines.modelDataQuality()))
        .modelQuality(translateFromResponse(driftCheckBaselines.modelQuality()))
        .build();
  }

  private static DriftCheckBias translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.DriftCheckBias bias) {
    return bias == null? null : DriftCheckBias.builder()
        .configFile(translateFromResponse(bias.configFile()))
        .postTrainingConstraints(translateFromResponse(bias.postTrainingConstraints()))
        .preTrainingConstraints(translateFromResponse(bias.preTrainingConstraints()))
        .build();
  }

  private static DriftCheckExplainability translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.DriftCheckExplainability explainability) {
    return explainability == null? null : DriftCheckExplainability.builder()
        .configFile(translateFromResponse(explainability.configFile()))
        .constraints(translateFromResponse(explainability.constraints()))
        .build();
  }

  private static DriftCheckModelDataQuality translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.DriftCheckModelDataQuality modelDataQuality) {
    return modelDataQuality == null? null : DriftCheckModelDataQuality.builder()
        .constraints(translateFromResponse(modelDataQuality.constraints()))
        .statistics(translateFromResponse(modelDataQuality.statistics()))
        .build();
  }

  private static FileSource translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.FileSource fileSource) {
    return fileSource == null? null : FileSource.builder()
        .contentDigest(fileSource.contentDigest())
        .contentType(fileSource.contentType())
        .s3Uri(fileSource.s3Uri())
        .build();
  }

  private static DriftCheckModelQuality translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.DriftCheckModelQuality modelQuality) {
    return modelQuality == null? null : DriftCheckModelQuality.builder()
        .constraints(translateFromResponse(modelQuality.constraints()))
        .statistics(translateFromResponse(modelQuality.statistics()))
        .build();
  }

  private static MetricsSource translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.MetricsSource metricsSource) {
    return metricsSource == null? null : MetricsSource.builder()
        .contentDigest(metricsSource.contentDigest())
        .contentType(metricsSource.contentType())
        .s3Uri(metricsSource.s3Uri())
        .build();
  }

  private static InferenceSpecification translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.InferenceSpecification inferenceSpecification) {
    return inferenceSpecification == null? null : InferenceSpecification.builder()
        .containers(sdkContainersToCfn(inferenceSpecification.containers()))
        .supportedContentTypes(inferenceSpecification.supportedContentTypes())
        .supportedRealtimeInferenceInstanceTypes(translateFromResponseSupportedRealtimeInferenceInstanceTypes(inferenceSpecification.supportedRealtimeInferenceInstanceTypes()))
        .supportedResponseMIMETypes(inferenceSpecification.supportedResponseMIMETypes())
        .supportedTransformInstanceTypes(translateFromResponseSupportedTransformInstanceTypes(inferenceSpecification.supportedTransformInstanceTypes()))
        .build();
  }

  private static List<String> translateFromResponseSupportedRealtimeInferenceInstanceTypes(
      List<software.amazon.awssdk.services.sagemaker.model.ProductionVariantInstanceType> supportedRealtimeInferenceInstanceTypes) {
    return supportedRealtimeInferenceInstanceTypes != null && isNotEmpty(supportedRealtimeInferenceInstanceTypes) ?
        supportedRealtimeInferenceInstanceTypes.stream()
            .map(e -> e.toString())
            .collect(Collectors.toList()): null;
  }

  private static List<String> translateFromResponseSupportedTransformInstanceTypes(
      List<software.amazon.awssdk.services.sagemaker.model.TransformInstanceType> supportedTransformInstanceTypes) {
    return supportedTransformInstanceTypes != null && isNotEmpty(supportedTransformInstanceTypes) ?
        supportedTransformInstanceTypes.stream()
            .map(e -> e.toString())
            .collect(Collectors.toList()): null;
  }

  private static MetadataProperties translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.MetadataProperties metadataProperties) {
    return metadataProperties == null? null : MetadataProperties.builder()
        .commitId(metadataProperties.commitId())
        .generatedBy(metadataProperties.generatedBy())
        .projectId(metadataProperties.projectId())
        .repository(metadataProperties.repository())
        .build();
  }

  private static ModelMetrics translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.ModelMetrics modelMetrics) {
    return modelMetrics == null? null : ModelMetrics.builder()
        .bias(translateFromResponse(modelMetrics.bias()))
        .explainability(translateFromResponse(modelMetrics.explainability()))
        .modelDataQuality(translateFromResponse(modelMetrics.modelDataQuality()))
        .modelQuality(translateFromResponse(modelMetrics.modelQuality()))
        .build();
  }

  private static Bias translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.Bias bias) {
    return bias == null? null : Bias.builder()
        .report(translateFromResponse(bias.report()))
        .preTrainingReport(translateFromResponse(bias.preTrainingReport()))
        .postTrainingReport(translateFromResponse(bias.postTrainingReport()))
        .build();
  }

  private static Explainability translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.Explainability explainability) {
    return explainability == null? null : Explainability.builder()
        .report(translateFromResponse(explainability.report()))
        .build();
  }

  private static ModelDataQuality translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.ModelDataQuality modelDataQuality) {
    return modelDataQuality == null? null : ModelDataQuality.builder()
        .constraints(translateFromResponse(modelDataQuality.constraints()))
        .statistics(translateFromResponse(modelDataQuality.statistics()))
        .build();
  }

  private static ModelQuality translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.ModelQuality modelQuality) {
    return modelQuality == null? null : ModelQuality.builder()
        .constraints(translateFromResponse(modelQuality.constraints()))
        .statistics(translateFromResponse(modelQuality.statistics()))
        .build();
  }

  private static SourceAlgorithmSpecification translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.SourceAlgorithmSpecification sourceAlgorithmSpecification) {
    return sourceAlgorithmSpecification == null? null : SourceAlgorithmSpecification.builder()
        .sourceAlgorithms(sdkSourceAlgorithmToCfn(sourceAlgorithmSpecification.sourceAlgorithms()))
        .build();
  }

  private static List<SourceAlgorithm> sdkSourceAlgorithmToCfn(
      List<software.amazon.awssdk.services.sagemaker.model.SourceAlgorithm> sourceAlgorithm) {
    if (sourceAlgorithm == null || isEmpty(sourceAlgorithm)) {
      return null;
    }

    return sourceAlgorithm.stream()
        .map(e -> SourceAlgorithm.builder()
            .algorithmName(e.algorithmName())
            .modelDataUrl(e.modelDataUrl())
            .build())
        .collect(Collectors.toList());
  }

  private static ValidationSpecification translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.ModelPackageValidationSpecification validationSpecification) {
    return validationSpecification == null? null : ValidationSpecification.builder()
        .validationProfiles(sdkValidationProfileToCfn(validationSpecification.validationProfiles()))
        .validationRole(validationSpecification.validationRole())
        .build();
  }

  private static List<ValidationProfile> sdkValidationProfileToCfn(
      List<software.amazon.awssdk.services.sagemaker.model.ModelPackageValidationProfile> validationProfiles) {
    if (validationProfiles == null) {
      return null;
    }
    return validationProfiles.stream()
        .map(e -> ValidationProfile.builder()
            .profileName(e.profileName())
            .transformJobDefinition(translateFromResponse(e.transformJobDefinition()))
            .build())
        .collect(Collectors.toList());
  }

  private static TransformJobDefinition translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.TransformJobDefinition transformJobDefinition) {
    return transformJobDefinition == null? null : TransformJobDefinition.builder()
        .batchStrategy(transformJobDefinition.batchStrategyAsString())
        .environment(translateMapOfStringsMapOfObjects(transformJobDefinition.environment()))
        .maxConcurrentTransforms(transformJobDefinition.maxConcurrentTransforms())
        .maxPayloadInMB(transformJobDefinition.maxPayloadInMB())
        .transformInput(translateFromResponse(transformJobDefinition.transformInput()))
        .transformOutput(translateFromResponse(transformJobDefinition.transformOutput()))
        .transformResources(translateFromResponse(transformJobDefinition.transformResources()))
        .build();
  }

  private static TransformInput translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.TransformInput transformInput) {
    return transformInput == null? null : TransformInput.builder()
        .splitType(transformInput.splitTypeAsString())
        .compressionType(transformInput.compressionTypeAsString())
        .contentType(transformInput.contentType())
        .dataSource(translateFromResponse(transformInput.dataSource()))
        .build();
  }

  private static DataSource translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.TransformDataSource dataSource) {
    return dataSource == null? null : DataSource.builder()
        .s3DataSource(translateFromResponse(dataSource.s3DataSource()))
        .build();
  }

  private static S3DataSource translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.TransformS3DataSource s3DataSource) {
    return s3DataSource == null? null : S3DataSource.builder()
        .s3DataType(s3DataSource.s3DataTypeAsString())
        .s3Uri(s3DataSource.s3Uri())
        .build();
  }


  private static TransformResources translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.TransformResources transformResources) {
    return transformResources == null? null : TransformResources.builder()
        .instanceCount(transformResources.instanceCount())
        .instanceType(transformResources.instanceTypeAsString())
        .volumeKmsKeyId(transformResources.volumeKmsKeyId())
        .build();
  }

  private static TransformOutput translateFromResponse(
      software.amazon.awssdk.services.sagemaker.model.TransformOutput transformOutput) {
    return transformOutput == null? null : TransformOutput.builder()
        .accept(transformOutput.accept())
        .assembleWith(transformOutput.assembleWithAsString())
        .kmsKeyId(transformOutput.kmsKeyId())
        .s3OutputPath(transformOutput.s3OutputPath())
        .build();
  }

  /**
     * Request to delete a resource
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
  static DeleteModelPackageRequest translateToDeleteRequest(final ResourceModel model) {
    if (model.getModelPackageName() != null)
      return DeleteModelPackageRequest.builder()
          .modelPackageName(model.getModelPackageName())
          .build();
    // covers versioned model package
    return DeleteModelPackageRequest.builder()
        .modelPackageName(model.getModelPackageArn())
        .build();
  }

    /**
     * Request to update properties of a previously created resource
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
  static UpdateModelPackageRequest translateToUpdateRequest(final ResourceModel model, List<String> customerMetadataPropertiesToRemove, List<AdditionalInferenceSpecificationDefinition> addnInferenceToAdd) {
    if (isEmpty(customerMetadataPropertiesToRemove)) {
      customerMetadataPropertiesToRemove = null;
    }
    if (isEmpty(addnInferenceToAdd)) {
      addnInferenceToAdd = null;
    }
    UpdateModelPackageRequest updateModelPackageRequest = UpdateModelPackageRequest.builder()
          .modelPackageArn(model.getModelPackageArn())
          .modelApprovalStatus(model.getModelApprovalStatus())
          .approvalDescription(model.getApprovalDescription())
          .customerMetadataProperties(translateMapOfObjectsToMapOfStrings(model.getCustomerMetadataProperties()))
          .customerMetadataPropertiesToRemove(customerMetadataPropertiesToRemove)
          .additionalInferenceSpecificationsToAdd(addnInferenceToAdd)
          .build();
    return updateModelPackageRequest;
  }

  /**
   * Request to list resources
   * @param nextToken token passed to the aws service list resources request
   * @return awsRequest the aws service request to list resources within aws account
   */
  static ListModelPackagesRequest translateToListRequest(final String nextToken, final ResourceModel model) {
    if (model.getModelPackageGroupName() != null)
    return ListModelPackagesRequest.builder()
        .modelPackageGroupName(model.getModelPackageGroupName())
        .nextToken(nextToken)
        .build();
    return ListModelPackagesRequest.builder()
        .nextToken(nextToken)
        .build();
  }

  /**
   * Translates resource objects from sdk into a resource model (primary identifier only)
   * @param awsResponse the aws service describe resource response
   * @return list of resource models
   */
  static List<ResourceModel> translateFromListResponse(final ListModelPackagesResponse awsResponse) {

    return Translator.streamOfOrEmpty(awsResponse.modelPackageSummaryList())
        .map(summary -> ResourceModel.builder()
            .creationTime(summary.creationTime().toString())
            .modelPackageArn(summary.modelPackageArn())
            .modelPackageName(summary.modelPackageName())
            .modelPackageStatus(summary.modelPackageStatus().toString())
            .modelPackageDescription(summary.modelPackageDescription())
            .modelApprovalStatus(summary.modelApprovalStatusAsString())
            .modelPackageGroupName(summary.modelPackageGroupName())
            .modelPackageVersion(summary.modelPackageVersion())
            .build())
        .collect(Collectors.toList());

  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }

  /**
   * Construct add tags request from list of tags and model package arn
   * @param tagsToAdd, list of tags to be added to the model package
   * @param arn, arn of the model package group to which tags have to be added.
   * @return awsRequest the aws service request to add tags to model package
   */
  static AddTagsRequest translateToAddTagsRequest(final List<Tag> tagsToAdd, String arn) {
    return AddTagsRequest.builder()
        .resourceArn(arn)
        .tags(tagsToAdd)
        .build();
  }

  /**
   * Construct delete tags request from list of tags and model package group arn
   * @param tagsToDelete, list of tags to be deleted from the model package group
   * @param arn, arn of the model package group from which tags have to be deleted.
   * @return awsRequest the aws service request to add tags to model package group
   */
  static DeleteTagsRequest translateToDeleteTagsRequest(final List<String> tagsToDelete, String arn) {
    return DeleteTagsRequest.builder()
        .resourceArn(arn)
        .tagKeys(tagsToDelete)
        .build();
  }

}
