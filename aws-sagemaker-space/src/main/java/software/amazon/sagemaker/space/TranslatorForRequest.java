package software.amazon.sagemaker.space;

import software.amazon.awssdk.services.sagemaker.model.CreateSpaceRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteSpaceRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeSpaceRequest;
import software.amazon.awssdk.services.sagemaker.model.ListSpacesRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateSpaceRequest;

import java.util.List;
import java.util.stream.Collectors;

final class TranslatorForRequest {

    private TranslatorForRequest() {}

    /**
     * Translates ResourceModel input to an aws sdk create resource request.
     *
     * @param model resource model
     * @return aws sdk create resource request
     */
    static CreateSpaceRequest translateToCreateRequest(final ResourceModel model) {
        return CreateSpaceRequest.builder()
                .domainId(model.getDomainId())
                .spaceName(model.getSpaceName())
                .spaceSettings(translateSpaceSettings(model.getSpaceSettings()))
                .tags(Translator.streamOfOrEmpty(model.getTags())
                        .map(t -> Tag.builder()
                                .key(t.getKey())
                                .value(t.getValue())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk read resource request.
     *
     * @param model resource model
     * @return aws sdk read resource request
     */
    static DescribeSpaceRequest translateToReadRequest(final ResourceModel model) {
        return DescribeSpaceRequest.builder()
                .domainId(model.getDomainId())
                .spaceName(model.getSpaceName())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk delete resource request.
     *
     * @param model resource model
     * @return aws sdk delete resource request
     */
    static DeleteSpaceRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteSpaceRequest.builder()
                .domainId(model.getDomainId())
                .spaceName(model.getSpaceName())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk update resource request.
     *
     * @param model resource model
     * @return aws sdk delete resource request
     */
    static UpdateSpaceRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateSpaceRequest.builder()
                .domainId(model.getDomainId())
                .spaceName(model.getSpaceName())
                .spaceSettings(translateSpaceSettings(model.getSpaceSettings()))
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk list resource request.
     *
     * @param nextToken token passed to the aws service describe resource request
     * @return list resource request
     */
    static ListSpacesRequest translateToListRequest(final String nextToken) {
        return ListSpacesRequest.builder().nextToken(nextToken).build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.SpaceSettings translateSpaceSettings(
            SpaceSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.SpaceSettings.builder()
                .jupyterServerAppSettings(translateJupyterServerAppSettings(origin.getJupyterServerAppSettings()))
                .kernelGatewayAppSettings(translateKernelGatewayAppSettings(origin.getKernelGatewayAppSettings()))
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.JupyterServerAppSettings translateJupyterServerAppSettings(
            JupyterServerAppSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.JupyterServerAppSettings.builder()
                .defaultResourceSpec(translateResourceSpec(origin.getDefaultResourceSpec()))
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.KernelGatewayAppSettings translateKernelGatewayAppSettings(
            KernelGatewayAppSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.KernelGatewayAppSettings.builder()
                .customImages(translateCustomImages(origin.getCustomImages()))
                .defaultResourceSpec(translateResourceSpec(origin.getDefaultResourceSpec()))
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.ResourceSpec translateResourceSpec(
            ResourceSpec origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.ResourceSpec.builder()
                .instanceType(origin.getInstanceType())
                .sageMakerImageArn(origin.getSageMakerImageArn())
                .sageMakerImageVersionArn(origin.getSageMakerImageVersionArn())
                .build();
    }

    private static List<software.amazon.awssdk.services.sagemaker.model.CustomImage> translateCustomImages(
            List<CustomImage> origin) {
        if (origin == null) {
            return null;
        }

        return Translator.streamOfOrEmpty(origin)
                .map(image -> software.amazon.awssdk.services.sagemaker.model.CustomImage.builder()
                        .appImageConfigName(image.getAppImageConfigName())
                        .imageName(image.getImageName())
                        .imageVersionNumber(image.getImageVersionNumber())
                        .build())
                .collect(Collectors.toList());
    }
}