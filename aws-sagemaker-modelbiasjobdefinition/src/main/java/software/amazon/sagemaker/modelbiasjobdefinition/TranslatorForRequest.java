package software.amazon.sagemaker.modelbiasjobdefinition;

import software.amazon.awssdk.services.sagemaker.model.CreateModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.ModelBiasAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.ModelBiasBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.ModelBiasJobInput;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.EndpointInput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringClusterConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringConstraintsResource;
import software.amazon.awssdk.services.sagemaker.model.MonitoringNetworkConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutputConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringResources;
import software.amazon.awssdk.services.sagemaker.model.MonitoringS3Output;
import software.amazon.awssdk.services.sagemaker.model.MonitoringStoppingCondition;
import software.amazon.awssdk.services.sagemaker.model.MonitoringGroundTruthS3Input;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.VpcConfig;

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
     * @return createModelBiasJobDefinitionRequest - service request to create a resource
     */
    static CreateModelBiasJobDefinitionRequest translateToCreateRequest(final ResourceModel model) {
        return CreateModelBiasJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .modelBiasAppSpecification(translate(model.getModelBiasAppSpecification()))
                .modelBiasBaselineConfig(translate(model.getModelBiasBaselineConfig()))
                .modelBiasJobInput(translate(model.getModelBiasJobInput()))
                .modelBiasJobOutputConfig(translate(model.getModelBiasJobOutputConfig()))
                .jobResources(translate(model.getJobResources()))
                .networkConfig(translate(model.getNetworkConfig()))
                .roleArn(model.getRoleArn())
                .stoppingCondition(translate(model.getStoppingCondition()))
                .tags(Translator.streamOfOrEmpty(model.getTags())
                        .map(curTag -> Tag.builder()
                                .key(curTag.getKey())
                                .value(curTag.getValue())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Request to read a resource
     * @param model resource model
     * @return describeModelBiasJobDefinitionRequest - the aws service request to describe a resource
     */
    static DescribeModelBiasJobDefinitionRequest translateToReadRequest(final ResourceModel model) {
        return DescribeModelBiasJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .build();
    }

    /**
     * Request to delete a resource
     * @param model resource model
     * @return deleteModelBiasJobDefinitionRequest the aws service request to delete a resource
     */
    static DeleteModelBiasJobDefinitionRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteModelBiasJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .build();
    }

    static ModelBiasAppSpecification translate(final software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasAppSpecification appSpec) {
        return appSpec == null ? null : ModelBiasAppSpecification.builder()
                .imageUri(appSpec.getImageUri())
                .configUri(appSpec.getConfigUri())
                .environment(translateMapOfObjectsToMapOfStrings(appSpec.getEnvironment()))
                .build();
    }

    static ModelBiasBaselineConfig translate(final software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasBaselineConfig baselineConfig) {
        return baselineConfig == null ? null : ModelBiasBaselineConfig.builder()
                .baseliningJobName(baselineConfig.getBaseliningJobName())
                .constraintsResource(translate(baselineConfig.getConstraintsResource()))
                .build();
    }

    static MonitoringConstraintsResource translate(final software.amazon.sagemaker.modelbiasjobdefinition.ConstraintsResource constraintsResource) {
        return constraintsResource == null ? null : MonitoringConstraintsResource.builder().s3Uri(constraintsResource.getS3Uri()).build();
    }

    static ModelBiasJobInput translate(final software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasJobInput jobInput) {
        return jobInput == null ? null : ModelBiasJobInput.builder()
                .endpointInput(translate(jobInput.getEndpointInput()))
                .groundTruthS3Input(translate(jobInput.getGroundTruthS3Input()))
                .build();
    }
    static EndpointInput translate(final software.amazon.sagemaker.modelbiasjobdefinition.EndpointInput endpointInput) {
        return endpointInput == null ? null : EndpointInput.builder()
                .endpointName(endpointInput.getEndpointName())
                .localPath(endpointInput.getLocalPath())
                .s3DataDistributionType(endpointInput.getS3DataDistributionType())
                .s3InputMode(endpointInput.getS3InputMode())
                .featuresAttribute(endpointInput.getFeaturesAttribute())
                .inferenceAttribute(endpointInput.getInferenceAttribute())
                .probabilityAttribute(endpointInput.getProbabilityAttribute())
                .probabilityThresholdAttribute(endpointInput.getProbabilityThresholdAttribute())
                .startTimeOffset(endpointInput.getStartTimeOffset())
                .endTimeOffset(endpointInput.getEndTimeOffset())
                .build();
    }
    static MonitoringOutputConfig translate(final software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.getKmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.getMonitoringOutputs()))
                .build();
    }

    static List<MonitoringOutput> translateOutput(final List<software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static MonitoringOutput translate(final software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.getS3Output()))
                .build();
    }

    static MonitoringS3Output translate(final software.amazon.sagemaker.modelbiasjobdefinition.S3Output s3Output) {
        return s3Output == null? null : MonitoringS3Output.builder()
                .localPath(s3Output.getLocalPath())
                .s3UploadMode(s3Output.getS3UploadMode())
                .s3Uri(s3Output.getS3Uri())
                .build();
    }

    static MonitoringResources translate(final software.amazon.sagemaker.modelbiasjobdefinition.MonitoringResources monitoringResources) {
        return monitoringResources == null? null : MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.getClusterConfig()))
                .build();
    }

    static MonitoringClusterConfig translate(final software.amazon.sagemaker.modelbiasjobdefinition.ClusterConfig clusterConfig) {
        return clusterConfig == null? null : MonitoringClusterConfig.builder()
                .instanceCount(clusterConfig.getInstanceCount())
                .instanceType(clusterConfig.getInstanceType())
                .volumeKmsKeyId(clusterConfig.getVolumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.getVolumeSizeInGB())
                .build();
    }

    static MonitoringNetworkConfig translate(final software.amazon.sagemaker.modelbiasjobdefinition.NetworkConfig networkConfig) {
        return networkConfig == null? null : MonitoringNetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.getEnableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.getEnableNetworkIsolation())
                .vpcConfig(translate(networkConfig.getVpcConfig()))
                .build();
    }

    static VpcConfig translate(final software.amazon.sagemaker.modelbiasjobdefinition.VpcConfig vpcConfig) {
        return vpcConfig == null? null : VpcConfig.builder()
                .securityGroupIds(vpcConfig.getSecurityGroupIds())
                .subnets(vpcConfig.getSubnets())
                .build();
    }

    static MonitoringStoppingCondition translate(final software.amazon.sagemaker.modelbiasjobdefinition.StoppingCondition stoppingCondition) {
        return stoppingCondition == null? null : MonitoringStoppingCondition.builder()
                .maxRuntimeInSeconds(stoppingCondition.getMaxRuntimeInSeconds())
                .build();
    }

    static MonitoringGroundTruthS3Input translate(final software.amazon.sagemaker.modelbiasjobdefinition.MonitoringGroundTruthS3Input s3Input) {
        return s3Input == null? null : MonitoringGroundTruthS3Input.builder()
                .s3Uri(s3Input.getS3Uri())
                .build();
    }

    static Map<String, String> translateMapOfObjectsToMapOfStrings(final Map<String, Object> mapOfObjects) {
        return mapOfObjects == null ? null : mapOfObjects.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue())
        );
    }

}
