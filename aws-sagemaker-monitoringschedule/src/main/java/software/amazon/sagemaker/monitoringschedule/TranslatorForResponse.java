package software.amazon.sagemaker.monitoringschedule;

import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleResponse;
import software.amazon.awssdk.services.sagemaker.model.EndpointInput;
import software.amazon.awssdk.services.sagemaker.model.ListMonitoringSchedulesResponse;
import software.amazon.awssdk.services.sagemaker.model.MonitoringAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.MonitoringBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringClusterConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringConstraintsResource;
import software.amazon.awssdk.services.sagemaker.model.MonitoringExecutionSummary;
import software.amazon.awssdk.services.sagemaker.model.MonitoringInput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringJobDefinition;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutput;
import software.amazon.awssdk.services.sagemaker.model.MonitoringOutputConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringResources;
import software.amazon.awssdk.services.sagemaker.model.MonitoringS3Output;
import software.amazon.awssdk.services.sagemaker.model.MonitoringScheduleConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringStatisticsResource;
import software.amazon.awssdk.services.sagemaker.model.MonitoringStoppingCondition;
import software.amazon.awssdk.services.sagemaker.model.NetworkConfig;
import software.amazon.awssdk.services.sagemaker.model.ScheduleConfig;
import software.amazon.awssdk.services.sagemaker.model.VpcConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {}

    /**
     * Translates resource object from sdk into a resource model
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeMonitoringScheduleResponse awsResponse) {
        return ResourceModel.builder()
                .creationTime(awsResponse.creationTime().toString())
                .endpointName(awsResponse.endpointName())
                .failureReason(awsResponse.failureReason())
                .lastModifiedTime(awsResponse.lastModifiedTime().toString())
                .lastMonitoringExecutionSummary(translate(awsResponse.lastMonitoringExecutionSummary()))
                .monitoringScheduleArn(awsResponse.monitoringScheduleArn())
                .monitoringScheduleConfig(translate(awsResponse.monitoringScheduleConfig()))
                .monitoringScheduleName(awsResponse.monitoringScheduleName())
                .monitoringScheduleStatus(awsResponse.monitoringScheduleStatus().toString())
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListMonitoringSchedulesResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.monitoringScheduleSummaries())
                .map(summary -> ResourceModel.builder()
                        .creationTime(summary.creationTime().toString())
                        .endpointName(summary.endpointName())
                        .monitoringScheduleArn(summary.monitoringScheduleArn())
                        .monitoringScheduleName(summary.monitoringScheduleName())
                        .monitoringScheduleStatus(summary.monitoringScheduleStatus().toString())
                        .lastModifiedTime(summary.lastModifiedTime().toString())
                        .build())
                .collect(Collectors.toList());
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringExecutionSummary translate(
            final MonitoringExecutionSummary monitoringExecutionSummary) {
        return monitoringExecutionSummary == null? null : software.amazon.sagemaker.monitoringschedule.MonitoringExecutionSummary.builder()
                .creationTime(monitoringExecutionSummary.creationTime().toString())
                .endpointName(monitoringExecutionSummary.endpointName())
                .failureReason(monitoringExecutionSummary.failureReason())
                .lastModifiedTime(monitoringExecutionSummary.lastModifiedTime().toString())
                .monitoringExecutionStatus(monitoringExecutionSummary.monitoringExecutionStatus().toString())
                .monitoringScheduleName(monitoringExecutionSummary.monitoringScheduleName())
                .processingJobArn(monitoringExecutionSummary.processingJobArn())
                .scheduledTime(monitoringExecutionSummary.scheduledTime().toString())
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringScheduleConfig translate(
            final MonitoringScheduleConfig monitoringScheduleConfig) {
        return monitoringScheduleConfig == null? null : software.amazon.sagemaker.monitoringschedule.MonitoringScheduleConfig.builder()
                .monitoringJobDefinition(translate(monitoringScheduleConfig.monitoringJobDefinition()))
                .monitoringJobDefinitionName(monitoringScheduleConfig.monitoringJobDefinitionName())
                .monitoringType(monitoringScheduleConfig.monitoringType().toString())
                .scheduleConfig(translate(monitoringScheduleConfig.scheduleConfig()))
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.ScheduleConfig translate(final ScheduleConfig config) {
        return config == null ? null : software.amazon.sagemaker.monitoringschedule.ScheduleConfig.builder()
                .scheduleExpression(config.scheduleExpression()).build();
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringJobDefinition translate(
            final MonitoringJobDefinition monitoringJobDefinition) {
        return monitoringJobDefinition == null? null : software.amazon.sagemaker.monitoringschedule.MonitoringJobDefinition.builder()
                .baselineConfig(translate(monitoringJobDefinition.baselineConfig()))
                .environment(translateMapOfStringsMapOfObjects(monitoringJobDefinition.environment()))
                .monitoringAppSpecification(translate(monitoringJobDefinition.monitoringAppSpecification()))
                .monitoringInputs(translateInput(monitoringJobDefinition.monitoringInputs()))
                .monitoringOutputConfig(translate(monitoringJobDefinition.monitoringOutputConfig()))
                .monitoringResources(translate(monitoringJobDefinition.monitoringResources()))
                .networkConfig(translate(monitoringJobDefinition.networkConfig()))
                .roleArn(monitoringJobDefinition.roleArn())
                .stoppingCondition(translate(monitoringJobDefinition.stoppingCondition()))
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.BaselineConfig translate(
            final MonitoringBaselineConfig monitoringBaselineConfig) {
        return monitoringBaselineConfig == null? null : software.amazon.sagemaker.monitoringschedule.BaselineConfig.builder()
                .constraintsResource(translate(monitoringBaselineConfig.constraintsResource()))
                .statisticsResource(translate(monitoringBaselineConfig.statisticsResource()))
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.ConstraintsResource translate(
            final MonitoringConstraintsResource constraintsResource) {
        return constraintsResource == null? null : software.amazon.sagemaker.monitoringschedule.ConstraintsResource.builder()
                .s3Uri(constraintsResource.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.StatisticsResource translate(
            final MonitoringStatisticsResource statisticsResource) {
        return statisticsResource == null? null : software.amazon.sagemaker.monitoringschedule.StatisticsResource.builder()
                .s3Uri(statisticsResource.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringAppSpecification translate(
            final MonitoringAppSpecification monitoringAppSpec) {
        return monitoringAppSpec == null ? null : software.amazon.sagemaker.monitoringschedule.MonitoringAppSpecification.builder()
                .containerArguments(monitoringAppSpec.containerArguments())
                .containerEntrypoint(monitoringAppSpec.containerEntrypoint())
                .imageUri(monitoringAppSpec.imageUri())
                .postAnalyticsProcessorSourceUri(monitoringAppSpec.postAnalyticsProcessorSourceUri())
                .recordPreprocessorSourceUri(monitoringAppSpec.recordPreprocessorSourceUri())
                .build();
    }

    static List<software.amazon.sagemaker.monitoringschedule.MonitoringInput> translateInput(final List<MonitoringInput> monitoringInputs) {
        return monitoringInputs == null ? null : monitoringInputs.stream()
                .map(monitoringInput -> translate(monitoringInput))
                .collect(Collectors.toList());
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringInput translate(final MonitoringInput monitoringInput) {
        return monitoringInput == null ? null : software.amazon.sagemaker.monitoringschedule.MonitoringInput.builder()
                .endpointInput(translate(monitoringInput.endpointInput()))
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.EndpointInput translate(final EndpointInput endpointInput) {
        return endpointInput == null ? null : software.amazon.sagemaker.monitoringschedule.EndpointInput.builder()
                .endpointName(endpointInput.endpointName())
                .localPath(endpointInput.localPath())
                .s3DataDistributionType(endpointInput.s3DataDistributionType().toString())
                .s3InputMode(endpointInput.s3InputMode().toString())
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringOutputConfig translate(final MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : software.amazon.sagemaker.monitoringschedule.MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.kmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.monitoringOutputs()))
                .build();
    }

    static List<software.amazon.sagemaker.monitoringschedule.MonitoringOutput> translateOutput(final List<MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringOutput translate(final MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : software.amazon.sagemaker.monitoringschedule.MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.s3Output()))
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.S3Output translate(final MonitoringS3Output s3Output) {
        return s3Output == null? null : software.amazon.sagemaker.monitoringschedule.S3Output.builder()
                .localPath(s3Output.localPath())
                .s3UploadMode(s3Output.s3UploadMode().toString())
                .s3Uri(s3Output.s3Uri())
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.MonitoringResources translate(final MonitoringResources monitoringResources) {
        return monitoringResources == null? null : software.amazon.sagemaker.monitoringschedule.MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.clusterConfig()))
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.ClusterConfig translate(final MonitoringClusterConfig clusterConfig) {
        return clusterConfig == null? null : software.amazon.sagemaker.monitoringschedule.ClusterConfig.builder()
                .instanceCount(clusterConfig.instanceCount())
                .instanceType(clusterConfig.instanceType().toString())
                .volumeKmsKeyId(clusterConfig.volumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.volumeSizeInGB())
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.NetworkConfig translate(final NetworkConfig networkConfig) {
        return networkConfig == null? null : software.amazon.sagemaker.monitoringschedule.NetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.enableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.enableNetworkIsolation())
                .vpcConfig(translate(networkConfig.vpcConfig()))
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.VpcConfig translate(final VpcConfig vpcConfig) {
        return vpcConfig == null? null : software.amazon.sagemaker.monitoringschedule.VpcConfig.builder()
                .securityGroupIds(vpcConfig.securityGroupIds())
                .subnets(vpcConfig.subnets())
                .build();
    }

    static software.amazon.sagemaker.monitoringschedule.StoppingCondition translate(final MonitoringStoppingCondition stoppingCondition) {
        return stoppingCondition == null? null : software.amazon.sagemaker.monitoringschedule.StoppingCondition.builder()
                .maxRuntimeInSeconds(stoppingCondition.maxRuntimeInSeconds())
                .build();
    }

    static Map<String, Object> translateMapOfStringsMapOfObjects(final Map<String, String> mapOfStrings) {
        return mapOfStrings == null ? null : mapOfStrings.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> (Object)e.getValue())
        );
    }

}
