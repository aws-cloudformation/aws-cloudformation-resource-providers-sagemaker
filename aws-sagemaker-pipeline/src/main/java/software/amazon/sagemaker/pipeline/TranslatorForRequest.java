package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.services.sagemaker.model.CreatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.DeletePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.ListPipelinesRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.PipelineDefinitionS3Location;
import software.amazon.awssdk.services.sagemaker.model.ParallelismConfiguration;

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
     * @return createPipelineRequest - service request to create a resource
     */
    static CreatePipelineRequest translateToCreateRequest(final ResourceModel model) {
        return CreatePipelineRequest.builder()
                .pipelineName(model.getPipelineName())
                .pipelineDisplayName(model.getPipelineDisplayName())
                .pipelineDefinition(model.getPipelineDefinition().getPipelineDefinitionBody())
                .pipelineDescription(model.getPipelineDescription())
                .roleArn(model.getRoleArn())
                .tags(Translator.streamOfOrEmpty(model.getTags())
                        .map(t -> Tag.builder()
                                .key(t.getKey())
                                .value(t.getValue())
                                .build())
                        .collect(Collectors.toList())
                )
                .pipelineDefinitionS3Location(convertPipelineDefinitionS3Location(
                        model.getPipelineDefinition().getPipelineDefinitionS3Location()))
                .parallelismConfiguration(convertParallelismConfiguration(model.getParallelismConfiguration()))
                .build();
    }

    /**
     * Request to read a resource
     * @param model resource model
     * @return describePipelineRequest - the aws service request to describe a resource
     */
    static DescribePipelineRequest translateToReadRequest(final ResourceModel model) {
        return DescribePipelineRequest.builder().pipelineName(model.getPipelineName()).build();
    }

    /**
     * Request to delete a resource
     * @param model resource model
     * @return deletePipelineRequest the aws service request to delete a resource
     */
    static DeletePipelineRequest translateToDeleteRequest(final ResourceModel model) {
        return DeletePipelineRequest.builder().pipelineName(model.getPipelineName()).build();
    }

    /**
     * Request to update properties of a previously created resource
     * @param model resource model
     * @return updatePipelineRequest the aws service request to modify a resource
     */
    static UpdatePipelineRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdatePipelineRequest.builder()
                .pipelineName(model.getPipelineName())
                .pipelineDisplayName(model.getPipelineDisplayName())
                .pipelineDescription(model.getPipelineDescription())
                .pipelineDefinition(model.getPipelineDefinition().getPipelineDefinitionBody())
                .roleArn(model.getRoleArn())
                .pipelineDefinitionS3Location(convertPipelineDefinitionS3Location(
                        model.getPipelineDefinition().getPipelineDefinitionS3Location()))
                .parallelismConfiguration(convertParallelismConfiguration(model.getParallelismConfiguration()))
                .build();
    }

    /**
     * Request to list properties of a previously created resource
     * @param nextToken token passed to the aws service describe resource request
     * @return awsRequest the aws service request to describe resources within aws account
     */
    static ListPipelinesRequest translateToListRequest(final String nextToken) {
        return ListPipelinesRequest.builder().nextToken(nextToken).build();
    }

    /**
     * Converter to create and return PipelineDefinitionS3Location
     * @param s3Location from the resource model
     * @return PipelineDefinitionS3Location for createPipelineRequest and updatePipelineRequest
     */
    private static PipelineDefinitionS3Location convertPipelineDefinitionS3Location(S3Location s3Location) {
        if(s3Location == null) {
            return null;
        }
        return PipelineDefinitionS3Location.builder()
                .bucket(s3Location.getBucket())
                .versionId(s3Location.getVersion())
                .objectKey(s3Location.getKey())
                .build();
    }

    /**
     * Converter to create and return ParallelismConfiguration
     * @param parallelismConfiguration from the resource model
     * @return ParallelismConfiguration for createPipelineRequest and updatePipelineRequest
     */
    private static ParallelismConfiguration convertParallelismConfiguration(
            software.amazon.sagemaker.pipeline.ParallelismConfiguration parallelismConfiguration) {
        if(parallelismConfiguration == null) {
            return null;
        }
        return ParallelismConfiguration.builder()
                .maxParallelExecutionSteps(parallelismConfiguration.getMaxParallelExecutionSteps())
                .build();
    }

}