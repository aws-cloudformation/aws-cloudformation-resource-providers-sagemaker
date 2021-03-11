package software.amazon.sagemaker.app;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AppType;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.function.Function;

public class AbstractTestBase {
    protected static final String TEST_USER_PROFILE_NAME = "testUserProfileName";
    protected static final String TEST_DOMAIN_ID = "testDomainId";
    protected static final String TEST_APP_TYPE = AppType.JUPYTER_SERVER.toString();
    protected static final String TEST_APP_NAME = "testAppName";
    protected static final String TEST_APP_ARN = "testAppArn";
    protected static final String TEST_INSTANCE_TYPE = "testInstanceType";
    protected static final String TEST_IMAGE_ARN = "testImageArn";
    protected static final String TEST_IMAGE_VERSION_ARN = "testImageVersionArn";
    protected static final String TEST_ERROR_MESSAGE = "test error message";
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
                .appName(TEST_APP_NAME)
                .appType(TEST_APP_TYPE)
                .domainId(TEST_DOMAIN_ID)
                .userProfileName(TEST_USER_PROFILE_NAME)
                .build();
    }
}