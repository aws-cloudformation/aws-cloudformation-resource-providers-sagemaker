package software.amazon.sagemaker.modelexplainabilityjobdefinition;

import software.amazon.awssdk.services.sagemaker.model.ModelExplainabilityAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.ModelExplainabilityBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.ModelExplainabilityJobInput;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelExplainabilityJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.EndpointInput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringClusterConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringConstraintsResource;
import software.amazon.awssdk.services.sagemaker.model.MonitoringNetworkConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutputConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringResources;
import software.amazon.awssdk.services.sagemaker.model.MonitoringS3Output;
import software.amazon.awssdk.services.sagemaker.model.MonitoringStoppingCondition;
import software.amazon.awssdk.services.sagemaker.model.VpcConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeModelExplainabilityJobDefinitionResponse awsResponse) {
        return ResourceModel.builder()
                .jobDefinitionArn(awsResponse.jobDefinitionArn())
                .jobDefinitionName(awsResponse.jobDefinitionName())
                .creationTime(awsResponse.creationTime().toString())
                .modelExplainabilityBaselineConfig(translate(awsResponse.modelExplainabilityBaselineConfig()))
                .modelExplainabilityAppSpecification(translate(awsResponse.modelExplainabilityAppSpecification()))
                .modelExplainabilityJobInput(translate(awsResponse.modelExplainabilityJobInput()))
                .modelExplainabilityJobOutputConfig(translate(awsResponse.modelExplainabilityJobOutputConfig()))
                .jobResources(translate(awsResponse.jobResources()))
                .networkConfig(translate(awsResponse.networkConfig()))
                .roleArn(awsResponse.roleArn())
                .stoppingCondition(translate(awsResponse.stoppingCondition()))
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityBaselineConfig translate(
            final ModelExplainabilityBaselineConfig baselineConfig) {
        return baselineConfig == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityBaselineConfig.builder()
                .baseliningJobName(baselineConfig.baseliningJobName())
                .constraintsResource(translate(baselineConfig.constraintsResource()))
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.ConstraintsResource translate(
            final MonitoringConstraintsResource constraintsResource) {
        return constraintsResource == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.ConstraintsResource.builder()
                .s3Uri(constraintsResource.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityAppSpecification translate(
            final ModelExplainabilityAppSpecification monitoringAppSpec) {
        return monitoringAppSpec == null ? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityAppSpecification.builder()
                .imageUri(monitoringAppSpec.imageUri())
                .configUri(monitoringAppSpec.configUri())
                .environment(translateMapOfStringsMapOfObjects(monitoringAppSpec.environment()))
                .build();
    }


    static software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityJobInput translate(final ModelExplainabilityJobInput monitoringInput) {
        return monitoringInput == null ? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.ModelExplainabilityJobInput.builder()
                .endpointInput(translate(monitoringInput.endpointInput()))
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.EndpointInput translate(final EndpointInput endpointInput) {
        return endpointInput == null ? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.EndpointInput.builder()
                .endpointName(endpointInput.endpointName())
                .localPath(endpointInput.localPath())
                .s3DataDistributionType(endpointInput.s3DataDistributionType().toString())
                .s3InputMode(endpointInput.s3InputMode().toString())
                .featuresAttribute(endpointInput.featuresAttribute())
                .inferenceAttribute(endpointInput.inferenceAttribute())
                .probabilityAttribute(endpointInput.probabilityAttribute())
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutputConfig translate(final MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.kmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.monitoringOutputs()))
                .build();
    }

    static List<software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutput> translateOutput(final List<MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutput translate(final MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.s3Output()))
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.S3Output translate(final MonitoringS3Output s3Output) {
        return s3Output == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.S3Output.builder()
                .localPath(s3Output.localPath())
                .s3UploadMode(s3Output.s3UploadMode().toString())
                .s3Uri(s3Output.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringResources translate(final MonitoringResources monitoringResources) {
        return monitoringResources == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.clusterConfig()))
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.ClusterConfig translate(final MonitoringClusterConfig clusterConfig) {
        return clusterConfig == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.ClusterConfig.builder()
                .instanceCount(clusterConfig.instanceCount())
                .instanceType(clusterConfig.instanceType().toString())
                .volumeKmsKeyId(clusterConfig.volumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.volumeSizeInGB())
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.NetworkConfig translate(final MonitoringNetworkConfig networkConfig) {
        return networkConfig == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.NetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.enableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.enableNetworkIsolation())
                .vpcConfig(translate(networkConfig.vpcConfig()))
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.VpcConfig translate(final VpcConfig vpcConfig) {
        return vpcConfig == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.VpcConfig.builder()
                .securityGroupIds(vpcConfig.securityGroupIds())
                .subnets(vpcConfig.subnets())
                .build();
    }

    static software.amazon.sagemaker.modelexplainabilityjobdefinition.StoppingCondition translate(final MonitoringStoppingCondition stoppingCondition) {
        return stoppingCondition == null? null : software.amazon.sagemaker.modelexplainabilityjobdefinition.StoppingCondition.builder()
                .maxRuntimeInSeconds(stoppingCondition.maxRuntimeInSeconds())
                .build();
    }

    static Map<String, Object> translateMapOfStringsMapOfObjects(final Map<String, String> mapOfStrings) {
        return mapOfStrings == null ? null : mapOfStrings.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> (Object)e.getValue())
        );
    }

}