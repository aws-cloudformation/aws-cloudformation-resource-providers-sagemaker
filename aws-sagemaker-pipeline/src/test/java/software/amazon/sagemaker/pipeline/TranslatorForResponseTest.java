package software.amazon.sagemaker.pipeline;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.Tag;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslatorForResponseTest {
    @Nested
    public class ReadResponseTest {
        @Test
        public void allFieldsPopulated() {
            final DescribePipelineResponse describePipelineResponse = DescribePipelineResponse.builder()
                    .pipelineName("pipeline-name")
                    .pipelineDisplayName("pipeline-display-name")
                    .pipelineDescription("description")
                    .roleArn("role")
                    .pipelineDefinition("pipeline-definition-body")
                    .parallelismConfiguration(
                            software.amazon.awssdk.services.sagemaker.model.ParallelismConfiguration.builder()
                                    .maxParallelExecutionSteps(50)
                                    .build())
                    .build();

            final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                    .tags(Tag.builder().value("value").key("key").build())
                    .build();

            final ResourceModel expected = ResourceModel.builder()
                    .pipelineName("pipeline-name")
                    .pipelineDefinition(PipelineDefinition.builder()
                            .pipelineDefinitionBody("pipeline-definition-body")
                            .build())
                    .pipelineDisplayName("pipeline-display-name")
                    .pipelineDescription("description")
                    .roleArn("role")
                    .parallelismConfiguration(ParallelismConfiguration.builder()
                            .maxParallelExecutionSteps(50)
                            .build())
                    .build();

            assertEquals(expected, TranslatorForResponse.translateFromReadResponse(describePipelineResponse));
        }

        @Test
        public void optionalFieldNotPopulated() {
            final DescribePipelineResponse describePipelineResponse = DescribePipelineResponse.builder()
                    .pipelineName("pipeline-name")
                    .roleArn("role")
                    .pipelineDefinition("pipeline-definition-body")
                    .build();

            final ListTagsResponse listTagsResponse = ListTagsResponse.builder()
                    .tags(Tag.builder().value("value").key("key").build())
                    .build();

            final ResourceModel expected = ResourceModel.builder()
                    .pipelineName("pipeline-name")
                    .pipelineDefinition(PipelineDefinition.builder()
                            .pipelineDefinitionBody("pipeline-definition-body")
                            .build())
                    .roleArn("role")
                    .build();

            assertEquals(expected, TranslatorForResponse.translateFromReadResponse(describePipelineResponse));
        }
    }
}