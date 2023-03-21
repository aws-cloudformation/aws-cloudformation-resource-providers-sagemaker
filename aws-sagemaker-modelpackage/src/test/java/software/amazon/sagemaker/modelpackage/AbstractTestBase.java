package software.amazon.sagemaker.modelpackage;

import com.google.common.collect.ImmutableList;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


public class AbstractTestBase {
  protected static final Instant TEST_CREATION_TIME = Instant.now();
  protected static final String TEST_MODEL_PACKAGE_NAME = "testModelPackageName";
  protected static final String TEST_MODEL_PACKAGE_ARN = "testModelPackageArn";
  protected static final String TEST_ERROR_MESSAGE = "test error message";
  protected static final String TEST_INTERNAL_ERROR_MESSAGE = "test internal error message";
  protected static final String TEST_VALIDATION_FAILURE_MESSAGE = "validation error detected";
  protected static final String MODEL_PACKAGE_NOT_EXISTS_ERROR_MESSAGE = "ModelPackage testModelPackageArn does not exist.";
  protected static final String MODEL_PACKAGE_ALREADY_EXISTS_ERROR_MESSAGE = "Project already exists: sample_arn";
  protected static final List<Tag> TEST_SDK_TAGS = ImmutableList.of(Tag.builder().key("key1").value("value1").build());
  protected static final List<software.amazon.sagemaker.modelpackage.Tag> TEST_CFN_MODEL_TAGS
      = ImmutableList.of(software.amazon.sagemaker.modelpackage.Tag.builder()
      .key("key1").value("value1").build());
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
