package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.services.sagemaker.SageMakerClient;

public class ClientBuilder {
    public static SageMakerClient getClient() {
        return SageMakerClient.builder().build();
    }
}
