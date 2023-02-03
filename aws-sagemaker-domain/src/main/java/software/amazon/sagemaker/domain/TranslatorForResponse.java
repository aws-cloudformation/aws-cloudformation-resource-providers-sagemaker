package software.amazon.sagemaker.domain;

import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.ListDomainsResponse;
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
    static ResourceModel translateFromReadResponse(final DescribeDomainResponse awsResponse) {
        return ResourceModel.builder()
                .domainArn(awsResponse.domainArn())
                .url(awsResponse.url())
                .appNetworkAccessType(awsResponse.appNetworkAccessTypeAsString())
                .authMode(awsResponse.authModeAsString())
                .defaultUserSettings(translateUserSettings(awsResponse.defaultUserSettings()))
                .defaultSpaceSettings(translateDefaultSpaceSettings(awsResponse.defaultSpaceSettings()))
                .domainName(awsResponse.domainName())
                .kmsKeyId(awsResponse.kmsKeyId())
                .subnetIds(awsResponse.subnetIds())
                .vpcId(awsResponse.vpcId())
                .domainId(awsResponse.domainId())
                .homeEfsFileSystemId(awsResponse.homeEfsFileSystemId())
                .singleSignOnManagedApplicationInstanceId(awsResponse.singleSignOnManagedApplicationInstanceId())
                .domainSettings(TranslatorForResponse.translateDomainSettings(awsResponse.domainSettings()))
                .appSecurityGroupManagement(awsResponse.appSecurityGroupManagementAsString())
                .securityGroupIdForDomainBoundary(awsResponse.securityGroupIdForDomainBoundary())
                .build();
    }

    /**
     * Translates the AWS SDK list response into a native resource model.
     *
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListDomainsResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.domains())
                .map(Domain -> ResourceModel.builder()
                        .domainId(Domain.domainId())
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

    private static DefaultSpaceSettings translateDefaultSpaceSettings(
            software.amazon.awssdk.services.sagemaker.model.DefaultSpaceSettings origin) {
        if (origin == null) {
            return null;
        }

        return DefaultSpaceSettings.builder()
                .executionRole(origin.executionRole())
                .jupyterServerAppSettings(translateJupyterServerAppSettings(origin.jupyterServerAppSettings()))
                .kernelGatewayAppSettings(translateKernelGatewayAppSettings(origin.kernelGatewayAppSettings()))
                .securityGroups(origin.hasSecurityGroups() ? origin.securityGroups() : null)
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

    private static software.amazon.sagemaker.domain.ResourceSpec translateResourceSpec(ResourceSpec origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.sagemaker.domain.ResourceSpec.builder()
                .instanceType(origin.instanceTypeAsString())
                .sageMakerImageArn(origin.sageMakerImageArn())
                .sageMakerImageVersionArn(origin.sageMakerImageVersionArn())
                .lifecycleConfigArn(origin.lifecycleConfigArn())
                .build();
    }

    private static List<CustomImage> translateCustomImages(
            List<software.amazon.awssdk.services.sagemaker.model.CustomImage> origin) {
        if (origin.isEmpty()) {
            return null;
        }

        return Translator.streamOfOrEmpty(origin)
                .map(image -> software.amazon.sagemaker.domain.CustomImage.builder()
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

        return software.amazon.sagemaker.domain.SharingSettings.builder()
                .notebookOutputOption(origin.notebookOutputOptionAsString())
                .s3KmsKeyId(origin.s3KmsKeyId())
                .s3OutputPath(origin.s3OutputPath())
                .build();
    }

    private static DomainSettings translateDomainSettings(software.amazon.awssdk.services.sagemaker.model.DomainSettings origin) {
        if (origin == null) {
            return null;
        }

        return DomainSettings.builder()
                .securityGroupIds(origin.securityGroupIds())
                .rStudioServerProDomainSettings(TranslatorForResponse.translateRStudioServerProDomainSettings(origin.rStudioServerProDomainSettings()))
                .build();
    }

    private static RStudioServerProDomainSettings translateRStudioServerProDomainSettings(
            software.amazon.awssdk.services.sagemaker.model.RStudioServerProDomainSettings origin) {
        if (origin == null) {
            return null;
        }

        return RStudioServerProDomainSettings.builder()
                .domainExecutionRoleArn(origin.domainExecutionRoleArn())
                .rStudioConnectUrl(origin.rStudioConnectUrl())
                .rStudioPackageManagerUrl(origin.rStudioPackageManagerUrl())
                .defaultResourceSpec(TranslatorForResponse.translateResourceSpec(origin.defaultResourceSpec()))
                .build();
    }
}
