package software.amazon.sagemaker.modelpackagegroup;

import com.google.common.collect.ImmutableList;
import org.json.JSONObject;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AbstractTestBase {
    protected static final Instant TEST_CREATION_TIME = Instant.now();
    protected static final String TEST_MODEL_PACKAGE_GROUP_ARN = "testModelPackageGroupArn";
    protected static final String TEST_MODEL_PACKAGE_GROUP_NAME = "testModelPackageGroupName";
    protected static final String TEST_ERROR_MESSAGE = "test error message";
    protected static final String MODEL_PACKAGE_GROUP_ALREADY_EXISTS_ERROR_MESSAGE = "Model Package Group already exists: sample_arn";
    protected static final String MODEL_PACKAGE_GROUP_NOT_EXISTS_ERROR_MESSAGE = "ModelPackageGroup sample_arn does not exist.";
    protected static final String CANNOT_FIND_MODEL_PACKAGE_GROUP_ERROR_MESSAGE = "Cannot find Model Package Group: sample_arn";
    protected static final String CANNOT_FIND_MODEL_PACKAGE_GROUP_POLICY_ERROR_MESSAGE = "Cannot find resource policy for: sample_arn";
    protected static Map<String, Object> TEST_MODEL_PACKAGE_GROUP_POLICY;
    protected static final String TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT = "{\"policy\": \"test model package group policy\"}";
    protected static final List<Tag> TEST_SDK_TAGS = ImmutableList.of(Tag.builder().key("key1").value("value1").build());
    protected static final List<software.amazon.sagemaker.modelpackagegroup.Tag> TEST_CFN_MODEL_TAGS
            = ImmutableList.of(software.amazon.sagemaker.modelpackagegroup.Tag.builder()
            .key("key1").value("value1").build());
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        TEST_MODEL_PACKAGE_GROUP_POLICY = getTestPolicy();
    }

    static Map<String, Object> getTestPolicy() {
         return new JSONObject(TEST_MODEL_PACKAGE_GROUP_POLICY_TEXT).toMap();
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
}
