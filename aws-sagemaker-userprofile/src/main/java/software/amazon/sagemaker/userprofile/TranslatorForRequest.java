package software.amazon.sagemaker.userprofile;

import software.amazon.awssdk.services.sagemaker.model.CreateUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeUserProfileRequest;
import software.amazon.awssdk.services.sagemaker.model.ListUserProfilesRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateUserProfileRequest;

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
    static CreateUserProfileRequest translateToCreateRequest(final ResourceModel model) {
        return CreateUserProfileRequest.builder()
                .domainId(model.getDomainId())
                .singleSignOnUserIdentifier(model.getSingleSignOnUserIdentifier())
                .singleSignOnUserValue(model.getSingleSignOnUserValue())
                .userProfileName(model.getUserProfileName())
                .userSettings(translateUserSettings(model.getUserSettings()))
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
    static DescribeUserProfileRequest translateToReadRequest(final ResourceModel model) {
        return DescribeUserProfileRequest.builder()
                .userProfileName(model.getUserProfileName())
                .domainId(model.getDomainId())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk delete resource request.
     *
     * @param model resource model
     * @return aws sdk delete resource request
     */
    static DeleteUserProfileRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteUserProfileRequest.builder()
                .userProfileName(model.getUserProfileName())
                .domainId(model.getDomainId())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk update resource request.
     *
     * @param model resource model
     * @return aws sdk delete resource request
     */
    static UpdateUserProfileRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateUserProfileRequest.builder()
                .domainId(model.getDomainId())
                .userProfileName(model.getUserProfileName())
                .userSettings(translateUserSettings(model.getUserSettings()))
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk list resource request.
     *
     * @param nextToken token passed to the aws service describe resource request
     * @return list resource request
     */
    static ListUserProfilesRequest translateToListRequest(final String nextToken) {
        return ListUserProfilesRequest.builder().nextToken(nextToken).build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.UserSettings translateUserSettings(
            UserSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.UserSettings.builder()
                .executionRole(origin.getExecutionRole())
                .jupyterServerAppSettings(translateJupyterServerAppSettings(origin.getJupyterServerAppSettings()))
                .kernelGatewayAppSettings(translateKernelGatewayAppSettings(origin.getKernelGatewayAppSettings()))
                .securityGroups(origin.getSecurityGroups())
                .sharingSettings(translateSharingSettings(origin.getSharingSettings()))
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

    private static software.amazon.awssdk.services.sagemaker.model.SharingSettings translateSharingSettings(
            SharingSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.SharingSettings.builder()
                .notebookOutputOption(origin.getNotebookOutputOption())
                .s3KmsKeyId(origin.getS3KmsKeyId())
                .s3OutputPath(origin.getS3OutputPath())
                .build();
    }
}