package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.services.sagemaker.model.CreateAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.FileSystemConfig;
import software.amazon.awssdk.services.sagemaker.model.KernelGatewayImageConfig;
import software.amazon.awssdk.services.sagemaker.model.KernelSpec;
import software.amazon.awssdk.services.sagemaker.model.ListAppImageConfigsRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateAppImageConfigRequest;

import java.util.stream.Collectors;

final class TranslatorForRequest {

    private TranslatorForRequest() {}

    /**
     * Translates ResourceModel input to an aws sdk create resource request
     *
     * @param model resource model
     * @return aws sdk create resource request
     */
    static CreateAppImageConfigRequest translateToCreateRequest(final ResourceModel model) {
        return CreateAppImageConfigRequest.builder()
                .appImageConfigName(model.getAppImageConfigName())
                .kernelGatewayImageConfig(translateKernelGatewayImageConfig(model.getKernelGatewayImageConfig()))
                .tags(Translator.streamOfOrEmpty(model.getTags())
                        .map(t -> Tag.builder()
                                .key(t.getKey())
                                .value(t.getValue())
                                .build())
                        .collect(Collectors.toList())
                ).build();
    }

    /**
     * Translates ResourceModel input to an aws sdk read resource request
     *
     * @param model resource model
     * @return aws sdk read resource request
     */
    static DescribeAppImageConfigRequest translateToReadRequest(final ResourceModel model) {
        return DescribeAppImageConfigRequest.builder()
                .appImageConfigName(model.getAppImageConfigName())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk delete resource request
     *
     * @param model resource model
     * @return aws sdk delete resource request
     */
    static DeleteAppImageConfigRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteAppImageConfigRequest.builder()
                .appImageConfigName(model.getAppImageConfigName())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk list resource request
     *
     * @param nextToken token passed to the aws service describe resource request
     * @return list resource request
     */
    static ListAppImageConfigsRequest translateToListRequest(final String nextToken) {
        return ListAppImageConfigsRequest.builder().nextToken(nextToken).build();
    }

    /**
     * Translates ResourceModel input to an aws sdk update resource request
     *
     * @param model resource model
     * @return update resource request
     */
    static UpdateAppImageConfigRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateAppImageConfigRequest.builder()
                .appImageConfigName(model.getAppImageConfigName())
                .kernelGatewayImageConfig(translateKernelGatewayImageConfig(model.getKernelGatewayImageConfig()))
                .build();
    }

    private static KernelGatewayImageConfig translateKernelGatewayImageConfig(
            software.amazon.sagemaker.appimageconfig.KernelGatewayImageConfig origin) {
        if (origin == null) {
            return null;
        }

        return KernelGatewayImageConfig.builder()
                .fileSystemConfig(origin.getFileSystemConfig() == null ? null :
                        FileSystemConfig.builder()
                                .defaultGid(origin.getFileSystemConfig().getDefaultGid())
                                .defaultUid(origin.getFileSystemConfig().getDefaultUid())
                                .mountPath(origin.getFileSystemConfig().getMountPath())
                                .build())
                .kernelSpecs(origin.getKernelSpecs().stream()
                        .map(kernelSpec -> KernelSpec.builder()
                                .displayName(kernelSpec.getDisplayName())
                                .name(kernelSpec.getName())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}