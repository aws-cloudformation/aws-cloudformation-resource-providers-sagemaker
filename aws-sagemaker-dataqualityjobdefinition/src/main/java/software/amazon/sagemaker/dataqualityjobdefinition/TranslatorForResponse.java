package software.amazon.sagemaker.dataqualityjobdefinition;

import software.amazon.awssdk.services.sagemaker.model.DataQualityAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.DataQualityBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.DataQualityJobInput;
import software.amazon.awssdk.services.sagemaker.model.DescribeDataQualityJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.EndpointInput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringClusterConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringConstraintsResource;
import software.amazon.awssdk.services.sagemaker.model.MonitoringNetworkConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutputConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringResources;
import software.amazon.awssdk.services.sagemaker.model.MonitoringS3Output;
import software.amazon.awssdk.services.sagemaker.model.MonitoringStatisticsResource;
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
    static ResourceModel translateFromReadResponse(final DescribeDataQualityJobDefinitionResponse awsResponse) {
        return ResourceModel.builder()
                .jobDefinitionArn(awsResponse.jobDefinitionArn())
                .jobDefinitionName(awsResponse.jobDefinitionName())
                .creationTime(awsResponse.creationTime().toString())
                .dataQualityBaselineConfig(translate(awsResponse.dataQualityBaselineConfig()))
                .dataQualityAppSpecification(translate(awsResponse.dataQualityAppSpecification()))
                .dataQualityJobInput(translate(awsResponse.dataQualityJobInput()))
                .dataQualityJobOutputConfig(translate(awsResponse.dataQualityJobOutputConfig()))
                .jobResources(translate(awsResponse.jobResources()))
                .networkConfig(translate(awsResponse.networkConfig()))
                .roleArn(awsResponse.roleArn())
                .stoppingCondition(translate(awsResponse.stoppingCondition()))
                .build();
    }


    static software.amazon.sagemaker.dataqualityjobdefinition.DataQualityBaselineConfig translate(
            final DataQualityBaselineConfig baselineConfig) {
        return baselineConfig == null? null : software.amazon.sagemaker.dataqualityjobdefinition.DataQualityBaselineConfig.builder()
                .baseliningJobName(baselineConfig.baseliningJobName())
                .constraintsResource(translate(baselineConfig.constraintsResource()))
                .statisticsResource(translate(baselineConfig.statisticsResource()))
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.ConstraintsResource translate(
            final MonitoringConstraintsResource constraintsResource) {
        return constraintsResource == null? null : software.amazon.sagemaker.dataqualityjobdefinition.ConstraintsResource.builder()
                .s3Uri(constraintsResource.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.StatisticsResource translate(
            final MonitoringStatisticsResource statisticsResource) {
        return statisticsResource == null? null : software.amazon.sagemaker.dataqualityjobdefinition.StatisticsResource.builder()
                .s3Uri(statisticsResource.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.DataQualityAppSpecification translate(
            final DataQualityAppSpecification monitoringAppSpec) {
        return monitoringAppSpec == null ? null : software.amazon.sagemaker.dataqualityjobdefinition.DataQualityAppSpecification.builder()
                .containerArguments(monitoringAppSpec.containerArguments())
                .containerEntrypoint(monitoringAppSpec.containerEntrypoint())
                .imageUri(monitoringAppSpec.imageUri())
                .postAnalyticsProcessorSourceUri(monitoringAppSpec.postAnalyticsProcessorSourceUri())
                .recordPreprocessorSourceUri(monitoringAppSpec.recordPreprocessorSourceUri())
                .environment(translateMapOfStringsMapOfObjects(monitoringAppSpec.environment()))
                .build();
    }


    static software.amazon.sagemaker.dataqualityjobdefinition.DataQualityJobInput translate(final DataQualityJobInput monitoringInput) {
        return monitoringInput == null ? null : software.amazon.sagemaker.dataqualityjobdefinition.DataQualityJobInput.builder()
                .endpointInput(translate(monitoringInput.endpointInput()))
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.EndpointInput translate(final EndpointInput endpointInput) {
        return endpointInput == null ? null : software.amazon.sagemaker.dataqualityjobdefinition.EndpointInput.builder()
                .endpointName(endpointInput.endpointName())
                .localPath(endpointInput.localPath())
                .s3DataDistributionType(endpointInput.s3DataDistributionType().toString())
                .s3InputMode(endpointInput.s3InputMode().toString())
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutputConfig translate(final MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.kmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.monitoringOutputs()))
                .build();
    }

    static List<software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutput> translateOutput(final List<MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutput translate(final MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.s3Output()))
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.S3Output translate(final MonitoringS3Output s3Output) {
        return s3Output == null? null : software.amazon.sagemaker.dataqualityjobdefinition.S3Output.builder()
                .localPath(s3Output.localPath())
                .s3UploadMode(s3Output.s3UploadMode().toString())
                .s3Uri(s3Output.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.MonitoringResources translate(final MonitoringResources monitoringResources) {
        return monitoringResources == null? null : software.amazon.sagemaker.dataqualityjobdefinition.MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.clusterConfig()))
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.ClusterConfig translate(final MonitoringClusterConfig clusterConfig) {
        return clusterConfig == null? null : software.amazon.sagemaker.dataqualityjobdefinition.ClusterConfig.builder()
                .instanceCount(clusterConfig.instanceCount())
                .instanceType(clusterConfig.instanceType().toString())
                .volumeKmsKeyId(clusterConfig.volumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.volumeSizeInGB())
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.NetworkConfig translate(final MonitoringNetworkConfig networkConfig) {
        return networkConfig == null? null : software.amazon.sagemaker.dataqualityjobdefinition.NetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.enableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.enableNetworkIsolation())
                .vpcConfig(translate(networkConfig.vpcConfig()))
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.VpcConfig translate(final VpcConfig vpcConfig) {
        return vpcConfig == null? null : software.amazon.sagemaker.dataqualityjobdefinition.VpcConfig.builder()
                .securityGroupIds(vpcConfig.securityGroupIds())
                .subnets(vpcConfig.subnets())
                .build();
    }

    static software.amazon.sagemaker.dataqualityjobdefinition.StoppingCondition translate(final MonitoringStoppingCondition stoppingCondition) {
        return stoppingCondition == null? null : software.amazon.sagemaker.dataqualityjobdefinition.StoppingCondition.builder()
                .maxRuntimeInSeconds(stoppingCondition.maxRuntimeInSeconds())
                .build();
    }

    static Map<String, Object> translateMapOfStringsMapOfObjects(final Map<String, String> mapOfStrings) {
        return mapOfStrings == null ? null : mapOfStrings.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> (Object)e.getValue())
        );
    }


}