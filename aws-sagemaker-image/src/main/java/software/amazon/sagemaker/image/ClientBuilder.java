package software.amazon.sagemaker.image;

import software.amazon.awssdk.services.sagemaker.SageMakerClient;

/**
 * Provides the client used by handlers to make service calls.
 */
public class ClientBuilder {
    public static SageMakerClient getClient() {
        return SageMakerClient.builder().build();
    }
}
