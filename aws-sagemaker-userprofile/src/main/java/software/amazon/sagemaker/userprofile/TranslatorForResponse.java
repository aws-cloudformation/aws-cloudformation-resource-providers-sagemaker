package software.amazon.sagemaker.userprofile;

import software.amazon.awssdk.services.sagemaker.model.DescribeUserProfileResponse;
import software.amazon.awssdk.services.sagemaker.model.ListUserProfilesResponse;
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
    static ResourceModel translateFromReadResponse(final DescribeUserProfileResponse awsResponse) {
        return ResourceModel.builder()
                .userProfileArn(awsResponse.userProfileArn())
                .userProfileName(awsResponse.userProfileName())
                .domainId(awsResponse.domainId())
                .singleSignOnUserIdentifier(awsResponse.singleSignOnUserIdentifier())
                .singleSignOnUserValue(awsResponse.singleSignOnUserValue())
                .userSettings(translateUserSettings(awsResponse.userSettings()))
                .build();
    }

    /**
     * Translates the AWS SDK list response into a native resource model.
     *
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListUserProfilesResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.userProfiles())
                .map(UserProfile -> ResourceModel.builder()
                        .domainId(UserProfile.domainId())
                        .userProfileName(UserProfile.userProfileName())
                        .build())
                .collect(Collectors.toList());
    }

    private static UserSettings translateUserSettings(
            software.amazon.awssdk.services.sagemaker.model.UserSettings origin) {
        if (origin == null) {
            return null;
        }

        return UserSettings.builder()
                .executionRole(origin.executionRole())
                .jupyterServerAppSettings(translateJupyterServerAppSettings(origin.jupyterServerAppSettings()))
                .kernelGatewayAppSettings(translateKernelGatewayAppSettings(origin.kernelGatewayAppSettings()))
                .securityGroups(origin.hasSecurityGroups() ? origin.securityGroups() : null)
                .sharingSettings(translateSharingSettings(origin.sharingSettings()))
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

    private static software.amazon.sagemaker.userprofile.ResourceSpec translateResourceSpec(ResourceSpec origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.sagemaker.userprofile.ResourceSpec.builder()
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
                .map(image -> software.amazon.sagemaker.userprofile.CustomImage.builder()
                        .appImageConfigName(image.appImageConfigName())
                        .imageName(image.imageName())
                        .imageVersionNumber(image.imageVersionNumber())
                        .build())
                .collect(Collectors.toList());
    }

    private static SharingSettings translateSharingSettings(
            software.amazon.awssdk.services.sagemaker.model.SharingSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.sagemaker.userprofile.SharingSettings.builder()
                .notebookOutputOption(origin.notebookOutputOptionAsString())
                .s3KmsKeyId(origin.s3KmsKeyId())
                .s3OutputPath(origin.s3OutputPath())
                .build();
    }
}
