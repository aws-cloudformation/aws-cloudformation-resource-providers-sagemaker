package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.List;
import com.google.common.collect.ImmutableList;

public class AbstractTestBase {

    protected static final String TEST_PIPELINE_NAME = "test-pipeline-name";
    protected static final String TEST_PIPELINE_ARN = "test-pipeline-arn";
    protected static final String TEST_PIPELINE_DISPLAY_NAME = "test-pipeline-display-name";
    protected static final String TEST_PIPELINE_DESCRIPTION = "test-pipeline-description";
    protected static final String TEST_PIPELINE_DEFINITION = "test-pipeline-definition";
    protected static final String TEST_ROLE_ARN = "test-role-arn";
    protected static final List<Tag> TEST_SDK_TAGS_K1_V1 = ImmutableList.of(Tag.builder().key("key1").value("val1").build());
    protected static final List<Tag> TEST_SDK_TAGS_K1_V2 = ImmutableList.of(Tag.builder().key("key1").value("val2").build());
    protected static final List<software.amazon.sagemaker.pipeline.Tag> TEST_CFN_MODEL_TAGS_K1_V1 = ImmutableList.of(software.amazon.sagemaker.pipeline.Tag.builder().key("key1").value("val1").build());
    protected static final List<software.amazon.sagemaker.pipeline.Tag> TEST_CFN_MODEL_TAGS_K1_V2 = ImmutableList.of(software.amazon.sagemaker.pipeline.Tag.builder().key("key1").value("val2").build());
    protected static final String TEST_ERROR_MESSAGE = "test error message";
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final String TEST_OPERATION = "test operation";
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    static ProxyClient<SageMakerClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final SageMakerClient sagemakerClient) {
        return new ProxyClient<SageMakerClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public SageMakerClient client() {
                return sagemakerClient;
            }
        };
    }

    static ResourceModel getResourceModel(final List<software.amazon.sagemaker.pipeline.Tag> tags) {
        ResourceModel.ResourceModelBuilder resourceModelBuilder = ResourceModel.builder()
                .pipelineName(TEST_PIPELINE_NAME)
                .pipelineDisplayName(TEST_PIPELINE_DISPLAY_NAME)
                .pipelineDescription(TEST_PIPELINE_DESCRIPTION)
                .pipelineDefinition(PipelineDefinition.builder()
                        .pipelineDefinitionBody(TEST_PIPELINE_DEFINITION).build())
                .roleArn(TEST_ROLE_ARN)
                .parallelismConfiguration(ParallelismConfiguration.builder()
                        .maxParallelExecutionSteps(2).build());
        if (!tags.isEmpty()) {
            resourceModelBuilder.tags(tags);
        }
        return resourceModelBuilder.build();
    }
}
