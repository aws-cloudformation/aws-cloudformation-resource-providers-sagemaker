package software.amazon.sagemaker.modelbiasjobdefinition;

import software.amazon.awssdk.services.sagemaker.model.ModelBiasAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.ModelBiasBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.ModelBiasJobInput;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelBiasJobDefinitionResponse;
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
import software.amazon.awssdk.services.sagemaker.model.MonitoringGroundTruthS3Input;

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
    static ResourceModel translateFromReadResponse(final DescribeModelBiasJobDefinitionResponse awsResponse) {
        return ResourceModel.builder()
                .jobDefinitionArn(awsResponse.jobDefinitionArn())
                .jobDefinitionName(awsResponse.jobDefinitionName())
                .creationTime(awsResponse.creationTime().toString())
                .modelBiasBaselineConfig(translate(awsResponse.modelBiasBaselineConfig()))
                .modelBiasAppSpecification(translate(awsResponse.modelBiasAppSpecification()))
                .modelBiasJobInput(translate(awsResponse.modelBiasJobInput()))
                .modelBiasJobOutputConfig(translate(awsResponse.modelBiasJobOutputConfig()))
                .jobResources(translate(awsResponse.jobResources()))
                .networkConfig(translate(awsResponse.networkConfig()))
                .roleArn(awsResponse.roleArn())
                .stoppingCondition(translate(awsResponse.stoppingCondition()))
                .build();
    }


    static software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasBaselineConfig translate(
            final ModelBiasBaselineConfig baselineConfig) {
        return baselineConfig == null? null : software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasBaselineConfig.builder()
                .baseliningJobName(baselineConfig.baseliningJobName())
                .constraintsResource(translate(baselineConfig.constraintsResource()))
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.ConstraintsResource translate(
            final MonitoringConstraintsResource constraintsResource) {
        return constraintsResource == null? null : software.amazon.sagemaker.modelbiasjobdefinition.ConstraintsResource.builder()
                .s3Uri(constraintsResource.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasAppSpecification translate(
            final ModelBiasAppSpecification monitoringAppSpec) {
        return monitoringAppSpec == null ? null : software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasAppSpecification.builder()
                .imageUri(monitoringAppSpec.imageUri())
                .configUri(monitoringAppSpec.configUri())
                .environment(translateMapOfStringsMapOfObjects(monitoringAppSpec.environment()))
                .build();
    }


    static software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasJobInput translate(final ModelBiasJobInput monitoringInput) {
        return monitoringInput == null ? null : software.amazon.sagemaker.modelbiasjobdefinition.ModelBiasJobInput.builder()
                .endpointInput(translate(monitoringInput.endpointInput()))
                .groundTruthS3Input(translate(monitoringInput.groundTruthS3Input()))
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.EndpointInput translate(final EndpointInput endpointInput) {
        return endpointInput == null ? null : software.amazon.sagemaker.modelbiasjobdefinition.EndpointInput.builder()
                .endpointName(endpointInput.endpointName())
                .localPath(endpointInput.localPath())
                .s3DataDistributionType(endpointInput.s3DataDistributionType().toString())
                .s3InputMode(endpointInput.s3InputMode().toString())
                .featuresAttribute(endpointInput.featuresAttribute())
                .inferenceAttribute(endpointInput.inferenceAttribute())
                .probabilityAttribute(endpointInput.probabilityAttribute())
                .probabilityThresholdAttribute(endpointInput.probabilityThresholdAttribute())
                .startTimeOffset(endpointInput.startTimeOffset())
                .endTimeOffset(endpointInput.endTimeOffset())
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutputConfig translate(final MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.kmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.monitoringOutputs()))
                .build();
    }

    static List<software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutput> translateOutput(final List<MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutput translate(final MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : software.amazon.sagemaker.modelbiasjobdefinition.MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.s3Output()))
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.S3Output translate(final MonitoringS3Output s3Output) {
        return s3Output == null? null : software.amazon.sagemaker.modelbiasjobdefinition.S3Output.builder()
                .localPath(s3Output.localPath())
                .s3UploadMode(s3Output.s3UploadMode().toString())
                .s3Uri(s3Output.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.MonitoringResources translate(final MonitoringResources monitoringResources) {
        return monitoringResources == null? null : software.amazon.sagemaker.modelbiasjobdefinition.MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.clusterConfig()))
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.ClusterConfig translate(final MonitoringClusterConfig clusterConfig) {
        return clusterConfig == null? null : software.amazon.sagemaker.modelbiasjobdefinition.ClusterConfig.builder()
                .instanceCount(clusterConfig.instanceCount())
                .instanceType(clusterConfig.instanceType().toString())
                .volumeKmsKeyId(clusterConfig.volumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.volumeSizeInGB())
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.NetworkConfig translate(final MonitoringNetworkConfig networkConfig) {
        return networkConfig == null? null : software.amazon.sagemaker.modelbiasjobdefinition.NetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.enableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.enableNetworkIsolation())
                .vpcConfig(translate(networkConfig.vpcConfig()))
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.VpcConfig translate(final VpcConfig vpcConfig) {
        return vpcConfig == null? null : software.amazon.sagemaker.modelbiasjobdefinition.VpcConfig.builder()
                .securityGroupIds(vpcConfig.securityGroupIds())
                .subnets(vpcConfig.subnets())
                .build();
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.StoppingCondition translate(final MonitoringStoppingCondition stoppingCondition) {
        return stoppingCondition == null? null : software.amazon.sagemaker.modelbiasjobdefinition.StoppingCondition.builder()
                .maxRuntimeInSeconds(stoppingCondition.maxRuntimeInSeconds())
                .build();
    }

    static Map<String, Object> translateMapOfStringsMapOfObjects(final Map<String, String> mapOfStrings) {
        return mapOfStrings == null ? null : mapOfStrings.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> (Object)e.getValue())
        );
    }

    static software.amazon.sagemaker.modelbiasjobdefinition.MonitoringGroundTruthS3Input translate(final MonitoringGroundTruthS3Input s3Input) {
        return s3Input == null? null : software.amazon.sagemaker.modelbiasjobdefinition.MonitoringGroundTruthS3Input.builder()
                .s3Uri(s3Input.s3Uri())
                .build();
    }

}