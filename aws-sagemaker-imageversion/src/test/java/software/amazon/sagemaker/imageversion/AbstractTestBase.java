package software.amazon.sagemaker.imageversion;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageVersionStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
  protected static final String TEST_IMAGE_ARN = "testImageArn";
  protected static final String TEST_IMAGE_NAME = "testImageName";
  protected static final Integer TEST_VERSION = 1;
  protected static final String TEST_IMAGE_VERSION_ARN = String.format("%s/%s", TEST_IMAGE_NAME, TEST_VERSION);
  protected static final String TEST_BASE_IMAGE = "testBaseImage";
  protected static final String TEST_CONTAINER_IMAGE = "testContainerImage";
  protected static final Instant TEST_CREATION_TIME = Instant.now();
  protected static final Instant TEST_LAST_MODIFIED_TIME = TEST_CREATION_TIME;
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
  }

  protected static ResourceModel createResourceModel() {
    return ResourceModel.builder()
            .imageName(TEST_IMAGE_NAME)
            .imageArn(TEST_IMAGE_ARN)
            .imageVersionArn(TEST_IMAGE_VERSION_ARN)
            .version(TEST_VERSION)
            .baseImage(TEST_BASE_IMAGE)
            .containerImage(TEST_CONTAINER_IMAGE)
            .build();
  }

  protected static DescribeImageVersionResponse createDescribeResponse(final ImageVersionStatus status) {
    return DescribeImageVersionResponse.builder()
            .imageArn(TEST_IMAGE_ARN)
            .imageVersionArn(TEST_IMAGE_VERSION_ARN)
            .version(TEST_VERSION)
            .imageVersionStatus(status)
            .baseImage(TEST_BASE_IMAGE)
            .containerImage(TEST_CONTAINER_IMAGE)
            .creationTime(TEST_CREATION_TIME)
            .lastModifiedTime(TEST_LAST_MODIFIED_TIME)
            .build();
  }

  static ProxyClient<SageMakerClient> MOCK_PROXY(
    final AmazonWebServicesClientProxy proxy,
    final SageMakerClient sageMakerClient) {
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
        return sageMakerClient;
      }
    };
  }
}
