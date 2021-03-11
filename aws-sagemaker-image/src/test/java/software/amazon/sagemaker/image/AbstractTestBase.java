package software.amazon.sagemaker.image;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {
  protected static final String TEST_IMAGE_ARN = "testImageArn";
  protected static final String TEST_IMAGE_NAME = "testImageName";
  protected static final String TEST_IMAGE_ROLE_ARN = "testImageRoleArn";
  protected static final String TEST_IMAGE_DISPLAY_NAME = "testImageDisplayName";
  protected static final String TEST_IMAGE_DESCRIPTION = "testImageDescription";
  protected static final Instant TEST_CREATION_TIME = Instant.now();
  protected static final Instant TEST_LAST_MODIFIED_TIME = TEST_CREATION_TIME;
  protected static final List<Tag> TEST_SDK_TAGS = ImmutableList.of(Tag.builder().key("key1").value("value1").build());
  protected static final List<software.amazon.sagemaker.image.Tag> TEST_CFN_MODEL_TAGS
          = ImmutableList.of(software.amazon.sagemaker.image.Tag.builder()
          .key("key1").value("value1").build());
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;

  static {
    MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
    logger = new LoggerProxy();
  }

  protected static ResourceModel createResourceModel(
          final String status,
          final boolean includeTags) {
    ResourceModel.ResourceModelBuilder resourceModelBuilder = ResourceModel.builder()
            .imageName(TEST_IMAGE_NAME)
            .imageArn(TEST_IMAGE_ARN)
            .imageRoleArn(TEST_IMAGE_ROLE_ARN)
            .imageDisplayName(TEST_IMAGE_DISPLAY_NAME)
            .imageDescription(TEST_IMAGE_DESCRIPTION);
    if (includeTags) {
      resourceModelBuilder.tags(TEST_CFN_MODEL_TAGS);
    }
    return resourceModelBuilder.build();
  }

  protected static DescribeImageResponse createDescribeImageResponse(final ImageStatus status) {
    return DescribeImageResponse.builder()
            .imageArn(TEST_IMAGE_ARN)
            .imageName(TEST_IMAGE_NAME)
            .roleArn(TEST_IMAGE_ROLE_ARN)
            .displayName(TEST_IMAGE_DISPLAY_NAME)
            .description(TEST_IMAGE_DESCRIPTION)
            .imageStatus(status)
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
