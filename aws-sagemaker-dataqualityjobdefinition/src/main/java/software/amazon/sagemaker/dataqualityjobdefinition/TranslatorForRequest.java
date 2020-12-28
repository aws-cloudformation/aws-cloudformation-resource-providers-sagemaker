package software.amazon.sagemaker.dataqualityjobdefinition;

import software.amazon.awssdk.services.sagemaker.model.CreateDataQualityJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DataQualityAppSpecification;
import software.amazon.awssdk.services.sagemaker.model.DataQualityBaselineConfig;
import software.amazon.awssdk.services.sagemaker.model.DataQualityJobInput;
import software.amazon.awssdk.services.sagemaker.model.DeleteDataQualityJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDataQualityJobDefinitionRequest;
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
     * @return createDataQualityJobDefinitionRequest - service request to create a resource
     */
    static CreateDataQualityJobDefinitionRequest translateToCreateRequest(final ResourceModel model) {
        return CreateDataQualityJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .dataQualityAppSpecification(translate(model.getDataQualityAppSpecification()))
                .dataQualityBaselineConfig(translate(model.getDataQualityBaselineConfig()))
                .dataQualityJobInput(translate(model.getDataQualityJobInput()))
                .dataQualityJobOutputConfig(translate(model.getDataQualityJobOutputConfig()))
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
     * @return describeDataQualityJobDefinitionRequest - the aws service request to describe a resource
     */
    static DescribeDataQualityJobDefinitionRequest translateToReadRequest(final ResourceModel model) {
        return DescribeDataQualityJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .build();
    }

    /**
     * Request to delete a resource
     * @param model resource model
     * @return deleteDataQualityJobDefinitionRequest the aws service request to delete a resource
     */
    static DeleteDataQualityJobDefinitionRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteDataQualityJobDefinitionRequest.builder()
                .jobDefinitionName(model.getJobDefinitionName())
                .build();
    }

    static DataQualityAppSpecification translate(final software.amazon.sagemaker.dataqualityjobdefinition.DataQualityAppSpecification appSpec) {
        return appSpec == null ? null : DataQualityAppSpecification.builder()
                .containerArguments(appSpec.getContainerArguments())
                .containerEntrypoint(appSpec.getContainerEntrypoint())
                .imageUri(appSpec.getImageUri())
                .postAnalyticsProcessorSourceUri(appSpec.getPostAnalyticsProcessorSourceUri())
                .recordPreprocessorSourceUri(appSpec.getRecordPreprocessorSourceUri())
                .environment(translateMapOfObjectsToMapOfStrings(appSpec.getEnvironment()))
                .build();
    }

    static DataQualityBaselineConfig translate(final software.amazon.sagemaker.dataqualityjobdefinition.DataQualityBaselineConfig baselineConfig) {
        return baselineConfig == null ? null : DataQualityBaselineConfig.builder()
                .baseliningJobName(baselineConfig.getBaseliningJobName())
                .constraintsResource(translate(baselineConfig.getConstraintsResource()))
                .statisticsResource(translate(baselineConfig.getStatisticsResource()))
                .build();
    }

    static MonitoringConstraintsResource translate(final software.amazon.sagemaker.dataqualityjobdefinition.ConstraintsResource constraintsResource) {
        return constraintsResource == null ? null : MonitoringConstraintsResource.builder().s3Uri(constraintsResource.getS3Uri()).build();
    }

    static MonitoringStatisticsResource translate(final software.amazon.sagemaker.dataqualityjobdefinition.StatisticsResource statisticsResource) {
        return statisticsResource == null ? null : MonitoringStatisticsResource.builder().s3Uri(statisticsResource.getS3Uri()).build();
    }


    static DataQualityJobInput translate(final software.amazon.sagemaker.dataqualityjobdefinition.DataQualityJobInput jobInput) {
        return jobInput == null ? null : DataQualityJobInput.builder()
                .endpointInput(translate(jobInput.getEndpointInput()))
                .build();
    }
    static EndpointInput translate(final software.amazon.sagemaker.dataqualityjobdefinition.EndpointInput endpointInput) {
        return endpointInput == null ? null : EndpointInput.builder()
                .endpointName(endpointInput.getEndpointName())
                .localPath(endpointInput.getLocalPath())
                .s3DataDistributionType(endpointInput.getS3DataDistributionType())
                .s3InputMode(endpointInput.getS3InputMode())
                .build();
    }
    static MonitoringOutputConfig translate(final software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutputConfig outputConfig) {
        return outputConfig == null? null : MonitoringOutputConfig.builder()
                .kmsKeyId(outputConfig.getKmsKeyId())
                .monitoringOutputs(translateOutput(outputConfig.getMonitoringOutputs()))
                .build();
    }

    static List<MonitoringOutput> translateOutput(final List<software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutput> monitoringOutputs) {
        return monitoringOutputs == null ? null : monitoringOutputs.stream()
                .map(monitoringOutput -> translate(monitoringOutput))
                .collect(Collectors.toList());
    }

    static MonitoringOutput translate(final software.amazon.sagemaker.dataqualityjobdefinition.MonitoringOutput monitoringOutput) {
        return monitoringOutput == null ? null : MonitoringOutput.builder()
                .s3Output(translate(monitoringOutput.getS3Output()))
                .build();
    }

    static MonitoringS3Output translate(final software.amazon.sagemaker.dataqualityjobdefinition.S3Output s3Output) {
        return s3Output == null? null : MonitoringS3Output.builder()
                .localPath(s3Output.getLocalPath())
                .s3UploadMode(s3Output.getS3UploadMode())
                .s3Uri(s3Output.getS3Uri())
                .build();
    }

    static MonitoringResources translate(final software.amazon.sagemaker.dataqualityjobdefinition.MonitoringResources monitoringResources) {
        return monitoringResources == null? null : MonitoringResources.builder()
                .clusterConfig(translate(monitoringResources.getClusterConfig()))
                .build();
    }

    static MonitoringClusterConfig translate(final software.amazon.sagemaker.dataqualityjobdefinition.ClusterConfig clusterConfig) {
        return clusterConfig == null? null : MonitoringClusterConfig.builder()
                .instanceCount(clusterConfig.getInstanceCount())
                .instanceType(clusterConfig.getInstanceType())
                .volumeKmsKeyId(clusterConfig.getVolumeKmsKeyId())
                .volumeSizeInGB(clusterConfig.getVolumeSizeInGB())
                .build();
    }

    static MonitoringNetworkConfig translate(final software.amazon.sagemaker.dataqualityjobdefinition.NetworkConfig networkConfig) {
        return networkConfig == null? null : MonitoringNetworkConfig.builder()
                .enableInterContainerTrafficEncryption(networkConfig.getEnableInterContainerTrafficEncryption())
                .enableNetworkIsolation(networkConfig.getEnableNetworkIsolation())
                .vpcConfig(translate(networkConfig.getVpcConfig()))
                .build();
    }

    static VpcConfig translate(final software.amazon.sagemaker.dataqualityjobdefinition.VpcConfig vpcConfig) {
        return vpcConfig == null? null : VpcConfig.builder()
                .securityGroupIds(vpcConfig.getSecurityGroupIds())
                .subnets(vpcConfig.getSubnets())
                .build();
    }

    static MonitoringStoppingCondition translate(final software.amazon.sagemaker.dataqualityjobdefinition.StoppingCondition stoppingCondition) {
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
