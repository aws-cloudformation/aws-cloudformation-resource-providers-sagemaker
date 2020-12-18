package software.amazon.sagemaker.pipeline;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.ListPipelinesResponse;

import java.util.List;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {}

    /**
     * Translates resource object from sdk into a resource model
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribePipelineResponse awsResponse) {
        return ResourceModel.builder()
                .pipelineName(awsResponse.pipelineName())
                .pipelineDefinition(
                        PipelineDefinition.builder()
                                .pipelineDefinitionBody(awsResponse.pipelineDefinition())
                                .build()
                )
                .pipelineDescription(awsResponse.pipelineDescription())
                .pipelineDisplayName(awsResponse.pipelineDisplayName())
                .roleArn(awsResponse.roleArn())
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
}
