package software.amazon.sagemaker.space;

import software.amazon.awssdk.services.sagemaker.SageMakerClient;

/**
 * Provides APIs to build the service client.
 */
public class ClientBuilder {
    public static SageMakerClient getClient() {
        return SageMakerClient.builder().build();
    }
}
