package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.services.sagemaker.model.DescribeAppImageConfigResponse;
import software.amazon.awssdk.services.sagemaker.model.ListAppImageConfigsResponse;

import java.util.List;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {}

    /**
     * Translates the AWS SDK read response into a native resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeAppImageConfigResponse awsResponse) {
        return ResourceModel.builder()
                .appImageConfigArn(awsResponse.appImageConfigArn())
                .appImageConfigName(awsResponse.appImageConfigName())
                .kernelGatewayImageConfig(translateToKernelGatewayImageConfig(awsResponse.kernelGatewayImageConfig()))
                .build();
    }

    /**
     * Translates the AWS SDK list response into a native resource model
     *
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListAppImageConfigsResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.appImageConfigs())
                .map(appImageConfig -> ResourceModel.builder()
                        .appImageConfigName(appImageConfig.appImageConfigName())
                        .build())
                .collect(Collectors.toList());
    }

    private static KernelGatewayImageConfig translateToKernelGatewayImageConfig(
            software.amazon.awssdk.services.sagemaker.model.KernelGatewayImageConfig origin) {
        if (origin == null) {
            return null;
        }

        return KernelGatewayImageConfig.builder()
                .fileSystemConfig(origin.fileSystemConfig() == null ? null :
                        FileSystemConfig.builder()
                        .defaultGid(origin.fileSystemConfig().defaultGid())
                        .defaultUid(origin.fileSystemConfig().defaultUid())
                        .mountPath(origin.fileSystemConfig().mountPath())
                        .build())
                .kernelSpecs(origin.kernelSpecs().stream()
                        .map(kernelSpec -> KernelSpec.builder()
                                .displayName(kernelSpec.displayName())
                                .name(kernelSpec.name())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
