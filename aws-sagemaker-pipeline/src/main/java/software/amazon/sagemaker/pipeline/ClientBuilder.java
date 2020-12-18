package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.services.sagemaker.SageMakerClient;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides APIs to build service client.
 */
public class ClientBuilder {

    public static SageMakerClient getClient()  {
        return SageMakerClient.create();
    }
}