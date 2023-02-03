package software.amazon.sagemaker.domain;

import software.amazon.awssdk.services.sagemaker.model.CreateDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.ListDomainsRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateDomainRequest;

import java.util.List;
import java.util.stream.Collectors;

final class TranslatorForRequest {

    private TranslatorForRequest() {}

    /**
     * Translates ResourceModel input to an aws sdk create resource request
     *
     * @param model resource model
     * @return aws sdk create resource request
     */
    static CreateDomainRequest translateToCreateRequest(final ResourceModel model) {
        return CreateDomainRequest.builder()
                .appNetworkAccessType(model.getAppNetworkAccessType())
                .authMode(model.getAuthMode())
                .defaultUserSettings(translateUserSettings(model.getDefaultUserSettings()))
                .defaultSpaceSettings(translateDefaultSpaceSettings(model.getDefaultSpaceSettings()))
                .domainName(model.getDomainName())
                .kmsKeyId(model.getKmsKeyId())
                .subnetIds(model.getSubnetIds())
                .vpcId(model.getVpcId())
                .tags(Translator.streamOfOrEmpty(model.getTags())
                        .map(t -> Tag.builder()
                                .key(t.getKey())
                                .value(t.getValue())
                                .build())
                        .collect(Collectors.toList()))
                .domainSettings(TranslatorForRequest.translateDomainSettings(model.getDomainSettings()))
                .appSecurityGroupManagement(model.getAppSecurityGroupManagement())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk read resource request
     *
     * @param model resource model
     * @return aws sdk read resource request
     */
    static DescribeDomainRequest translateToReadRequest(final ResourceModel model) {
        return DescribeDomainRequest.builder()
                .domainId(model.getDomainId())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk delete resource request
     *
     * @param model resource model
     * @return aws sdk delete resource request
     */
    static DeleteDomainRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteDomainRequest.builder()
                .domainId(model.getDomainId())
                .build();
    }
    /**
     * Translates ResourceModel input to an aws sdk update resource request
     *
     * @param model resource model
     * @return update resource request
     */
    static UpdateDomainRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateDomainRequest.builder()
                .domainId(model.getDomainId())
                .defaultUserSettings(translateUserSettings(model.getDefaultUserSettings()))
                .defaultSpaceSettings(translateDefaultSpaceSettings(model.getDefaultSpaceSettings()))
                .domainSettingsForUpdate(TranslatorForRequest.translateDomainSettingsForUpdate(model.getDomainSettings()))
                .appSecurityGroupManagement(model.getAppSecurityGroupManagement())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk list resource request
     *
     * @param nextToken token passed to the aws service describe resource request
     * @return list resource request
     */
    static ListDomainsRequest translateToListRequest(final String nextToken) {
        return ListDomainsRequest.builder().nextToken(nextToken).build();
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
                .rStudioServerProAppSettings(translateRStudioServerProAppSettings(origin.getRStudioServerProAppSettings()))
                .rSessionAppSettings(translateRSessionAppSettings(origin.getRSessionAppSettings()))
                .securityGroups(origin.getSecurityGroups())
                .sharingSettings(translateSharingSettings(origin.getSharingSettings()))
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.DefaultSpaceSettings translateDefaultSpaceSettings(
            DefaultSpaceSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.DefaultSpaceSettings.builder()
                .executionRole(origin.getExecutionRole())
                .jupyterServerAppSettings(translateJupyterServerAppSettings(origin.getJupyterServerAppSettings()))
                .kernelGatewayAppSettings(translateKernelGatewayAppSettings(origin.getKernelGatewayAppSettings()))
                .securityGroups(origin.getSecurityGroups())
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

    private static software.amazon.awssdk.services.sagemaker.model.RStudioServerProAppSettings translateRStudioServerProAppSettings(
            RStudioServerProAppSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.RStudioServerProAppSettings.builder()
                .accessStatus(origin.getAccessStatus())
                .userGroup(origin.getUserGroup())
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.RSessionAppSettings translateRSessionAppSettings(
            RSessionAppSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.RSessionAppSettings.builder()
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
                .lifecycleConfigArn(origin.getLifecycleConfigArn())
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

    private static software.amazon.awssdk.services.sagemaker.model.DomainSettings translateDomainSettings(DomainSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.DomainSettings.builder()
                .securityGroupIds(origin.getSecurityGroupIds())
                .rStudioServerProDomainSettings(TranslatorForRequest.translateRStudioServerProDomainSettings(origin.getRStudioServerProDomainSettings()))
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.RStudioServerProDomainSettings translateRStudioServerProDomainSettings(
            RStudioServerProDomainSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.RStudioServerProDomainSettings.builder()
                .domainExecutionRoleArn(origin.getDomainExecutionRoleArn())
                .rStudioConnectUrl(origin.getRStudioConnectUrl())
                .rStudioPackageManagerUrl(origin.getRStudioPackageManagerUrl())
                .defaultResourceSpec(TranslatorForRequest.translateResourceSpec(origin.getDefaultResourceSpec()))
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.DomainSettingsForUpdate translateDomainSettingsForUpdate(DomainSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.DomainSettingsForUpdate.builder()
                .securityGroupIds(origin.getSecurityGroupIds())
                .rStudioServerProDomainSettingsForUpdate(TranslatorForRequest.translateRStudioServerProDomainSettingsForUpdate(origin.getRStudioServerProDomainSettings()))
                .build();
    }

    private static software.amazon.awssdk.services.sagemaker.model.RStudioServerProDomainSettingsForUpdate translateRStudioServerProDomainSettingsForUpdate(
            RStudioServerProDomainSettings origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.awssdk.services.sagemaker.model.RStudioServerProDomainSettingsForUpdate.builder()
                .domainExecutionRoleArn(origin.getDomainExecutionRoleArn())
                .rStudioConnectUrl(origin.getRStudioConnectUrl())
                .rStudioPackageManagerUrl(origin.getRStudioPackageManagerUrl())
                .defaultResourceSpec(TranslatorForRequest.translateResourceSpec(origin.getDefaultResourceSpec()))
                .build();
    }
}