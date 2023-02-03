package software.amazon.sagemaker.space;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.function.Function;

public class AbstractTestBase {
    protected static final String TEST_SPACE_NAME = "testSpaceName";
    protected static final String TEST_SPACE_ARN = "testSpaceArn";
    protected static final String TEST_DOMAIN_ID = "testDomainId";
    protected static final String TEST_STATUS = "testStatus";
    protected static final String TEST_INSTANCE_TYPE = "testInstanceType";
    protected static final String TEST_IMAGE_ARN = "testImageArn";
    protected static final String TEST_IMAGE_VERSION_ARN = "testImageVersionArn";
    protected static final String TEST_IMAGE_NAME = "testImgName";
    protected static final String TEST_APP_IMAGE_CONFIG_NAME = "testAppImageConfigName";
    protected static final int TEST_IMAGE_VERSION_NUMBER = 7;
    protected static final String TEST_FAILURE_REASON = "testFailureReason";
    protected static final String TEST_ERROR_MESSAGE = "test error message";
    protected static final Instant TEST_TIME = Instant.now();
    protected static final Credentials MOCK_CREDENTIALS;
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
            public SageMakerClient client() {
                return sagemakerClient;
            }
        };
    }

    protected static ResourceModel getRequestResourceModel() {
        return ResourceModel.builder()
                .spaceName(TEST_SPACE_NAME)
                .domainId(TEST_DOMAIN_ID)
                .build();
    }
}