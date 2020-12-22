package software.amazon.sagemaker.modelexplainabilityjobdefinition;

import software.amazon.awssdk.services.sagemaker.model.CreateModelExplainabilityJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.ModelExplainabilityAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.ModelExplainabilityBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.ModelExplainabilityJobInput;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelExplainabilityJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelExplainabilityJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.EndpointInput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringClusterConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringConstraintsResource;
import software.amazon.awssdk.services.sagemaker.model.MonitoringNetworkConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutputConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringResources;
import software.amazon.awssdk.services.sagemaker.model.MonitoringS3Output;
import software.amazon.awssdk.services.sagemaker.model.MonitoringStoppingCondition;
import software.amazon.awssdk.services.sagemaker.model.ProblemType;
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
     * @return createModelExplainabilityJobDefinitionRequest - service request to create a resource
     */
    static CreateModelExplainabilityJobDefinitionRequest translateToCreateRequest(final ResourceModel model) {
        return CreateModelExplainabilityJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .modelExplainabilityAppSpecification(translate(model.getModelExplainabilityAppSpecification()))
                .modelExplainabilityBaselineConfig(translate(model.getModelExplainabilityBaselineConfig()))
                .modelExplainabilityJobInput(translate(model.getModelExplainabilityJobInput()))
                .modelExplainabilityJobOutputConfig(translate(model.getModelExplainabilityJobOutputConfig()))
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
     * @return describeModelExplainabilityJobDefinitionRequest - the aws service request to describe a resource
     */
    static DescribeModelExplainabilityJobDefinitionRequest translateToReadRequest(final ResourceModel model) {
        return DescribeModelExplainabilityJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .build();
    }

    /**
     * Request to delete a resource
     * @param model resource model
     * @return deleteModelExplainabilityJobDefinitionRequest the aws service request to delete a resource
     */
    static DeleteModelExplainabilityJobDefinitionRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteModelExplainabilityJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .build();
    }

    static ModelExplainabilityAppSpecification translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityAppSpecification appSpec) {
        return appSpec == null ? null : ModelExplainabilityAppSpecification.builder()
                .imageUri(appSpec.getImageUri())
                .configUri(appSpec.getConfigUri())
                .environment(translateMapOfObjectsToMapOfStrings(appSpec.getEnvironment()))
                .build();
    }

    static ModelExplainabilityBaselineConfig translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityBaselineConfig baselineConfig) {
        return baselineConfig == null ? null : ModelExplainabilityBaselineConfig.builder()
                .baseliningJobName(baselineConfig.getBaseliningJobName())
                .constraintsResource(translate(baselineConfig.getConstraintsResource()))
                .build();
    }

    static MonitoringConstraintsResource translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.ConstraintsResource constraintsResource) {
        return constraintsResource == null ? null : MonitoringConstraintsResource.builder().s3Uri(constraintsResource.getS3Uri()).build();
    }

    static ModelExplainabilityJobInput translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityJobInput jobInput) {
        return jobInput == null ? null : ModelExplainabilityJobInput.builder()
                .endpointInput(translate(jobInput.getEndpointInput()))
                .build();
    }
    static EndpointInput translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.EndpointInput endpointInput) {
        return endpointInput == null ? null : EndpointInput.builder()
                .endpointName(endpointInput.getEndpointName())
                .localPath(endpointInput.getLocalPath())
                .s3DataDistributionType(endpointInput.getS3DataDistributionType())
                .s3InputMode(endpointInput.getS3InputMode())
                .featuresAttribute(endpointInput.getFeaturesAttribute())
                .inferenceAttribute(endpointInput.getInferenceAttribute())
                .probabilityAttribute(endpointInput.getProbabilityAttribute())
                .build();
    }
    static MonitoringOutputConfig translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.getKmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.getMonitoringOutputs()))
                .build();
    }

    static List<MonitoringOutput> translateOutput(final List<software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static MonitoringOutput translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.getS3Output()))
                .build();
    }

    static MonitoringS3Output translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.S3Output s3Output) {
        return s3Output == null? null : MonitoringS3Output.builder()
                .localPath(s3Output.getLocalPath())
                .s3UploadMode(s3Output.getS3UploadMode())
                .s3Uri(s3Output.getS3Uri())
                .build();
    }

    static MonitoringResources translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringResources monitoringResources) {
        return monitoringResources == null? null : MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.getClusterConfig()))
                .build();
    }

    static MonitoringClusterConfig translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.ClusterConfig clusterConfig) {
        return clusterConfig == null? null : MonitoringClusterConfig.builder()
                .instanceCount(clusterConfig.getInstanceCount())
                .instanceType(clusterConfig.getInstanceType())
                .volumeKmsKeyId(clusterConfig.getVolumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.getVolumeSizeInGB())
                .build();
    }

    static MonitoringNetworkConfig translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.NetworkConfig networkConfig) {
        return networkConfig == null? null : MonitoringNetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.getEnableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.getEnableNetworkIsolation())
                .vpcConfig(translate(networkConfig.getVpcConfig()))
                .build();
    }

    static VpcConfig translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.VpcConfig vpcConfig) {
        return vpcConfig == null? null : VpcConfig.builder()
                .securityGroupIds(vpcConfig.getSecurityGroupIds())
                .subnets(vpcConfig.getSubnets())
                .build();
    }

    static MonitoringStoppingCondition translate(final software.amazon.sagemaker.modelexplainabilityjobdefinition.StoppingCondition stoppingCondition) {
        return stoppingCondition == null? null : MonitoringStoppingCondition.builder()
                .maxRuntimeInSeconds(stoppingCondition.getMaxRuntimeInSeconds())
                .build();
    }

    static Map<String, String> translateMapOfObjectsToMapOfStrings(final Map<String, Object> mapOfObjects) {
        return mapOfObjects == null ? null : mapOfObjects.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue())
        );
    }

}
