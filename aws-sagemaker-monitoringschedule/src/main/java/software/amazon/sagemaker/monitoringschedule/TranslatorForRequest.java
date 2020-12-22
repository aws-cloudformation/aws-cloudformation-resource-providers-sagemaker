package software.amazon.sagemaker.monitoringschedule;

import software.amazon.awssdk.services.sagemaker.model.CreateMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMonitoringScheduleRequest;
import software.amazon.awssdk.services.sagemaker.model.EndpointInput;
import software.amazon.awssdk.services.sagemaker.model.ListMonitoringSchedulesRequest;
import software.amazon.awssdk.services.sagemaker.model.MonitoringAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.MonitoringBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringClusterConfig;
import software.amazon.awssdk.services.sagemaker.model.MonitoringConstraintsResource;
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
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateMonitoringScheduleRequest;
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
     * @return createMonitoringScheduleRequest - service request to create a resource
     */
    static CreateMonitoringScheduleRequest translateToCreateRequest(final ResourceModel model) {
        return CreateMonitoringScheduleRequest.builder()
                .monitoringScheduleName(model.getMonitoringScheduleName())
                .monitoringScheduleConfig(translate(model.getMonitoringScheduleConfig()))
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
     * @return describeMonitoringScheduleRequest - the aws service request to describe a resource
     */
    static DescribeMonitoringScheduleRequest translateToReadRequest(final ResourceModel model) {
        return DescribeMonitoringScheduleRequest.builder()
                .monitoringScheduleName(model.getMonitoringScheduleName())
                .build();
    }

    /**
     * Request to delete a resource
     * @param model resource model
     * @return deleteMonitoringScheduleRequest the aws service request to delete a resource
     */
    static DeleteMonitoringScheduleRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteMonitoringScheduleRequest.builder()
                .monitoringScheduleName(model.getMonitoringScheduleName())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     * @param model resource model
     * @return updateMonitoringScheduleRequest the aws service request to modify a resource
     */
    static UpdateMonitoringScheduleRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateMonitoringScheduleRequest.builder()
                .monitoringScheduleName(model.getMonitoringScheduleName())
                .monitoringScheduleConfig(translate(model.getMonitoringScheduleConfig()))
                .build();
    }


    /**
     * Request to list properties of a previously created resource
     * @param nextToken token passed to the aws service describe resource request
     * @return awsRequest the aws service request to describe resources within aws account
     */
    static ListMonitoringSchedulesRequest translateToListRequest(final String nextToken) {
        return ListMonitoringSchedulesRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    /**
     * Converts MonitoringScheduleConfig from a CFN resource model to a Sagemaker SDK object.
     * @param config config from CFN resource provider.
     * @return Sagemaker MonitoringScheduleConfig object.
     */
    static MonitoringScheduleConfig translate(final software.amazon.sagemaker.monitoringschedule.MonitoringScheduleConfig config) {
        return config == null ? null : MonitoringScheduleConfig.builder()
                .monitoringJobDefinition(translate(config.getMonitoringJobDefinition()))
                .monitoringJobDefinitionName(config.getMonitoringJobDefinitionName())
                .monitoringType(config.getMonitoringType())
                .scheduleConfig(translate(config.getScheduleConfig()))
                .build();
    }

    static ScheduleConfig translate(final software.amazon.sagemaker.monitoringschedule.ScheduleConfig config) {
        return config == null ? null : ScheduleConfig.builder().scheduleExpression(config.getScheduleExpression()).build();
    }

    static MonitoringJobDefinition translate(final software.amazon.sagemaker.monitoringschedule.MonitoringJobDefinition jobDefinition) {
        return jobDefinition == null ? null : MonitoringJobDefinition.builder()
                .baselineConfig(translate(jobDefinition.getBaselineConfig()))
                .environment(translateMapOfObjectsToMapOfStrings(jobDefinition.getEnvironment()))
                .monitoringAppSpecification(translate(jobDefinition.getMonitoringAppSpecification()))
                .monitoringInputs(translate(jobDefinition.getMonitoringInputs()))
                .monitoringOutputConfig(translate(jobDefinition.getMonitoringOutputConfig()))
                .monitoringResources(translate(jobDefinition.getMonitoringResources()))
                .networkConfig(translate(jobDefinition.getNetworkConfig()))
                .roleArn(jobDefinition.getRoleArn())
                .stoppingCondition(translate(jobDefinition.getStoppingCondition()))
                .build();
    }

    static MonitoringBaselineConfig translate(final software.amazon.sagemaker.monitoringschedule.BaselineConfig baselineConfig) {
        return baselineConfig == null ? null : MonitoringBaselineConfig.builder()
                .constraintsResource(translate(baselineConfig.getConstraintsResource()))
                .statisticsResource(translate(baselineConfig.getStatisticsResource()))
                .build();
    }

    static MonitoringConstraintsResource translate(final software.amazon.sagemaker.monitoringschedule.ConstraintsResource constraintsResource) {
        return constraintsResource == null ? null : MonitoringConstraintsResource.builder().s3Uri(constraintsResource.getS3Uri()).build();
    }

    static MonitoringStatisticsResource translate(final software.amazon.sagemaker.monitoringschedule.StatisticsResource statisticsResource) {
        return statisticsResource == null ? null : MonitoringStatisticsResource.builder().s3Uri(statisticsResource.getS3Uri()).build();
    }

    static MonitoringAppSpecification translate(final software.amazon.sagemaker.monitoringschedule.MonitoringAppSpecification monitoringAppSpec) {
        return monitoringAppSpec == null ? null : MonitoringAppSpecification.builder()
                .containerArguments(monitoringAppSpec.getContainerArguments())
                .containerEntrypoint(monitoringAppSpec.getContainerEntrypoint())
                .imageUri(monitoringAppSpec.getImageUri())
                .postAnalyticsProcessorSourceUri(monitoringAppSpec.getPostAnalyticsProcessorSourceUri())
                .recordPreprocessorSourceUri(monitoringAppSpec.getRecordPreprocessorSourceUri())
                .build();
    }

    static List<MonitoringInput> translate(final List<software.amazon.sagemaker.monitoringschedule.MonitoringInput> monitoringInputs) {
        return monitoringInputs == null ? null : monitoringInputs.stream()
                .map(monitoringInput -> translate(monitoringInput))
                .collect(Collectors.toList());
    }

    static MonitoringInput translate(final software.amazon.sagemaker.monitoringschedule.MonitoringInput monitoringInput) {
        return monitoringInput == null ? null : MonitoringInput.builder()
                .endpointInput(translate(monitoringInput.getEndpointInput()))
                .build();
    }

    static EndpointInput translate(final software.amazon.sagemaker.monitoringschedule.EndpointInput endpointInput) {
        return endpointInput == null ? null : EndpointInput.builder()
                .endpointName(endpointInput.getEndpointName())
                .localPath(endpointInput.getLocalPath())
                .s3DataDistributionType(endpointInput.getS3DataDistributionType())
                .s3InputMode(endpointInput.getS3InputMode())
                .build();
    }

    static MonitoringOutputConfig translate(final software.amazon.sagemaker.monitoringschedule.MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.getKmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.getMonitoringOutputs()))
                .build();
    }

    static List<MonitoringOutput> translateOutput(final List<software.amazon.sagemaker.monitoringschedule.MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static MonitoringOutput translate(final software.amazon.sagemaker.monitoringschedule.MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.getS3Output()))
                .build();
    }

    static MonitoringS3Output translate(final software.amazon.sagemaker.monitoringschedule.S3Output s3Output) {
        return s3Output == null? null : MonitoringS3Output.builder()
                .localPath(s3Output.getLocalPath())
                .s3UploadMode(s3Output.getS3UploadMode())
                .s3Uri(s3Output.getS3Uri())
                .build();
    }

    static MonitoringResources translate(final software.amazon.sagemaker.monitoringschedule.MonitoringResources monitoringResources) {
        return monitoringResources == null? null : MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.getClusterConfig()))
                .build();
    }

    static MonitoringClusterConfig translate(final software.amazon.sagemaker.monitoringschedule.ClusterConfig clusterConfig) {
        return clusterConfig == null? null : MonitoringClusterConfig.builder()
                .instanceCount(clusterConfig.getInstanceCount())
                .instanceType(clusterConfig.getInstanceType())
                .volumeKmsKeyId(clusterConfig.getVolumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.getVolumeSizeInGB())
                .build();
    }

    static NetworkConfig translate(final software.amazon.sagemaker.monitoringschedule.NetworkConfig networkConfig) {
        return networkConfig == null? null : NetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.getEnableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.getEnableNetworkIsolation())
                .vpcConfig(translate(networkConfig.getVpcConfig()))
                .build();
    }

    static VpcConfig translate(final software.amazon.sagemaker.monitoringschedule.VpcConfig vpcConfig) {
        return vpcConfig == null? null : VpcConfig.builder()
                .securityGroupIds(vpcConfig.getSecurityGroupIds())
                .subnets(vpcConfig.getSubnets())
                .build();
    }

    static MonitoringStoppingCondition translate(final software.amazon.sagemaker.monitoringschedule.StoppingCondition stoppingCondition) {
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