package software.amazon.sagemaker.space;

import software.amazon.awssdk.services.sagemaker.model.DescribeSpaceResponse;
import software.amazon.awssdk.services.sagemaker.model.ListSpacesResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceSpec;

import java.util.List;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {}

    /**
     * Translates the AWS SDK read response into a native resource model.
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeSpaceResponse awsResponse) {
        return ResourceModel.builder()
                .spaceArn(awsResponse.spaceArn())
                .spaceName(awsResponse.spaceName())
                .domainId(awsResponse.domainId())
                .spaceSettings(translateSpaceSettings(awsResponse.spaceSettings()))
                .build();
    }

    /**
     * Translates the AWS SDK list response into a native resource model.
     *
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListSpacesResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.spaces())
                .map(Space -> ResourceModel.builder()
                        .domainId(Space.domainId())
                        .spaceName(Space.spaceName())
                        .build())
                .collect(Collectors.toList());
    }

    private static SpaceSettings translateSpaceSettings(
            software.amazon.awssdk.services.sagemaker.model.SpaceSettings origin) {
        if (origin == null) {
            return null;
        }

        return SpaceSettings.builder()
                .jupyterServerAppSettings(translateJupyterServerAppSettings(origin.jupyterServerAppSettings()))
                .kernelGatewayAppSettings(translateKernelGatewayAppSettings(origin.kernelGatewayAppSettings()))
                .build();
    }

    private static JupyterServerAppSettings translateJupyterServerAppSettings(
            software.amazon.awssdk.services.sagemaker.model.JupyterServerAppSettings origin) {
        if (origin == null) {
            return null;
        }

        return JupyterServerAppSettings.builder()
                .defaultResourceSpec(translateResourceSpec(origin.defaultResourceSpec()))
                .build();
    }

    private static KernelGatewayAppSettings translateKernelGatewayAppSettings(
            software.amazon.awssdk.services.sagemaker.model.KernelGatewayAppSettings origin) {
        if (origin == null) {
            return null;
        }

        return KernelGatewayAppSettings.builder()
                .customImages(translateCustomImages(origin.customImages()))
                .defaultResourceSpec(translateResourceSpec(origin.defaultResourceSpec()))
                .build();
    }

    private static software.amazon.sagemaker.space.ResourceSpec translateResourceSpec(ResourceSpec origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.sagemaker.space.ResourceSpec.builder()
                .instanceType(origin.instanceTypeAsString())
                .sageMakerImageArn(origin.sageMakerImageArn())
                .sageMakerImageVersionArn(origin.sageMakerImageVersionArn())
                .build();
    }

    private static List<CustomImage> translateCustomImages(
            List<software.amazon.awssdk.services.sagemaker.model.CustomImage> origin) {
        if (origin.isEmpty()) {
            return null;
        }

        return Translator.streamOfOrEmpty(origin)
                .map(image -> software.amazon.sagemaker.space.CustomImage.builder()
                        .appImageConfigName(image.appImageConfigName())
                        .imageName(image.imageName())
                        .imageVersionNumber(image.imageVersionNumber())
                        .build())
                .collect(Collectors.toList());
    }
}
