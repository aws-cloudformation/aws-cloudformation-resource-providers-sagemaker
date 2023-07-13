package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.services.sagemaker.model.DescribePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.ListPipelinesResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;

import java.util.List;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {}

    /**
     * Translates resource object from sdk into a resource model
     * @param describePipelineResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribePipelineResponse describePipelineResponse) {
        return ResourceModel.builder()
                .pipelineName(describePipelineResponse.pipelineName())
                .pipelineDefinition(
                        PipelineDefinition.builder()
                                .pipelineDefinitionBody(describePipelineResponse.pipelineDefinition())
                                .build()
                )
                .pipelineDescription(describePipelineResponse.pipelineDescription())
                .pipelineDisplayName(describePipelineResponse.pipelineDisplayName())
                .roleArn(describePipelineResponse.roleArn())
                .parallelismConfiguration(convertParallelismConfiguration(describePipelineResponse.parallelismConfiguration()))
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListPipelinesResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.pipelineSummaries())
                .map(summary -> ResourceModel.builder()
                        .pipelineName(summary.pipelineName())
                        .pipelineDisplayName(summary.pipelineDisplayName())
                        .pipelineDescription(summary.pipelineDescription())
                        .roleArn(summary.roleArn())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Converter to create and return ParallelismConfiguration for the resource model
     * @param parallelismConfiguration from the aws service describe resource response
     *
     * @return ParallelismConfiguration for resource model
     */
    private static ParallelismConfiguration convertParallelismConfiguration(
            software.amazon.awssdk.services.sagemaker.model.ParallelismConfiguration parallelismConfiguration) {
        if(parallelismConfiguration == null) {
            return null;
        }
        return ParallelismConfiguration.builder()
                .maxParallelExecutionSteps(parallelismConfiguration.maxParallelExecutionSteps())
                .build();
    }
}
