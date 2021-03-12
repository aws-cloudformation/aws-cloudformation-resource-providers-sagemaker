package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.function.Function;

public class AbstractTestBase {
    protected static final String TEST_APP_IMAGE_CONFIG_NAME = "testAppImageConfigName";
    protected static final String TEST_APP_IMAGE_CONFIG_ARN = "testAppImageConfigArn";
    protected static final String TEST_KERNEL_NAME = "testKernel";
    protected static final String TEST_KERNEL_DISPLAY = "testKernelDisplay";
    protected static final String TEST_MOUNT_PATH = "testMountPath";
    protected static final int TEST_DEFAULT_GID = 1;
    protected static final int TEST_DEFAULT_UID = 2;
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
                .appImageConfigName(TEST_APP_IMAGE_CONFIG_NAME)
                .build();
    }
}