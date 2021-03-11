package software.amazon.sagemaker.domain;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DomainDetails;
import software.amazon.awssdk.services.sagemaker.model.ListDomainsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Collections;
import java.util.function.Function;

public class AbstractTestBase {
    protected static final String TEST_DOMAIN_NAME = "testDomainName";
    protected static final String TEST_DOMAIN_ID = "testDomainId";
    protected static final String TEST_DOMAIN_ARN = String.format("testAppArn/%s", TEST_DOMAIN_ID);
    protected static final String TEST_URL = "testUrl";
    protected static final String TEST_STATUS = "testStatus";
    protected static final String TEST_AUTH_MODE = "testAuth";
    protected static final String TEST_APP_NETWORK_TYPE = "testAppNetwork";
    protected static final String TEST_VPC_ID = "testVpc";
    protected static final String TEST_SUBNET_ID = "testSubnet";
    protected static final String TEST_KMS = "testKMS";
    protected static final String TEST_EFS_ID = "testEfs";
    protected static final String TEST_SSO_MANAGED_APP = "testSSOManaged";
    protected static final String TEST_INSTANCE_TYPE = "testInstanceType";
    protected static final String TEST_IMAGE_ARN = "testImageArn";
    protected static final String TEST_IMAGE_VERSION_ARN = "testImageVersionArn";
    protected static final String TEST_NB_OUTPUT = "testNBOutput";
    protected static final String TEST_S3_KMS = "testS3KMS";
    protected static final String TEST_S3_OUTPUT = "testS3Output";
    protected static final String TEST_SECURITY_GROUP = "testSecGroup";
    protected static final String TEST_ROLE = "testRole";
    protected static final String TEST_IMAGE_NAME = "testImgName";
    protected static final String TEST_APP_IMAGE_CONFIG_NAME = "testAppImageConfigName";
    protected static final int TEST_IMAGE_VERSION_NUMBER = 7;
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

    static ResourceModel getPostCreationResourceModel() {
        return ResourceModel.builder()
                .domainId(TEST_DOMAIN_ID)
                .build();
    }
}