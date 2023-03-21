package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.services.sagemaker.SageMakerClient;

/**
 * Provides APIs to build service client.
 */
public class ClientBuilder {

  public static SageMakerClient getClient() {

    return SageMakerClient.builder().build();

  }

}