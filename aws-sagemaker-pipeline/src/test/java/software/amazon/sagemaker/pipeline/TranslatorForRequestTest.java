package software.amazon.sagemaker.pipeline;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sagemaker.model.CreatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.PipelineDefinitionS3Location;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslatorForRequestTest {
    @Nested
    public class CreateRequestTest {
        @Test
        public void withPipelineDefinitionBody() {
            final ResourceModel resourceModel = ResourceModel.builder()
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
                    .tags(Collections.singletonList(Tag.builder()
                            .key("key")
                            .value("value")
                            .build()))
                    .build();

            final CreatePipelineRequest expected = CreatePipelineRequest.builder()
                    .pipelineName("pipeline-name")
                    .pipelineDisplayName("pipeline-display-name")
                    .pipelineDescription("description")
                    .roleArn("role")
                    .pipelineDefinition("pipeline-definition-body")
                    .parallelismConfiguration(
                            software.amazon.awssdk.services.sagemaker.model.ParallelismConfiguration.builder()
                                    .maxParallelExecutionSteps(50)
                                    .build())
                    .tags(Collections.singletonList(software.amazon.awssdk.services.sagemaker.model.Tag.builder()
                            .key("key")
                            .value("value")
                            .build()))
                    .build();

            assertEquals(expected, TranslatorForRequest.translateToCreateRequest(resourceModel));
        }


        @Test
        public void withS3Location() {
            final ResourceModel resourceModel = ResourceModel.builder()
                    .pipelineName("pipeline-name")
                    .pipelineDefinition(PipelineDefinition.builder()
                            .pipelineDefinitionS3Location(S3Location.builder()
                                    .bucket("bucket")
                                    .key("key")
                                    .version("version")
                                    .build())
                            .build())
                    .roleArn("role")
                    .build();

            final CreatePipelineRequest expected = CreatePipelineRequest.builder()
                    .pipelineName("pipeline-name")
                    .roleArn("role")
                    .pipelineDefinitionS3Location(PipelineDefinitionS3Location.builder()
                            .bucket("bucket")
                            .objectKey("key")
                            .versionId("version")
                            .build())
                    .tags(Collections.emptyList())
                    .build();

            assertEquals(expected, TranslatorForRequest.translateToCreateRequest(resourceModel));
        }
    }

    @Nested
    public class UpdateRequestTest {
        @Test
        public void withPipelineDefinitionBody() {
            final ResourceModel resourceModel = ResourceModel.builder()
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

            final UpdatePipelineRequest expected = UpdatePipelineRequest.builder()
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

            assertEquals(expected, TranslatorForRequest.translateToUpdateRequest(resourceModel));
        }


        @Test
        public void withS3Location() {
            final ResourceModel resourceModel = ResourceModel.builder()
                    .pipelineName("pipeline-name")
                    .pipelineDefinition(PipelineDefinition.builder()
                            .pipelineDefinitionS3Location(S3Location.builder()
                                    .bucket("bucket")
                                    .key("key")
                                    .version("version")
                                    .build())
                            .build())
                    .roleArn("role")
                    .build();

            final UpdatePipelineRequest expected = UpdatePipelineRequest.builder()
                    .pipelineName("pipeline-name")
                    .roleArn("role")
                    .pipelineDefinitionS3Location(PipelineDefinitionS3Location.builder()
                            .bucket("bucket")
                            .objectKey("key")
                            .versionId("version")
                            .build())
                    .build();

            assertEquals(expected, TranslatorForRequest.translateToUpdateRequest(resourceModel));
        }
    }
}