package software.amazon.sagemaker.app;

import software.amazon.awssdk.services.sagemaker.model.CreateAppRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppRequest;
import software.amazon.awssdk.services.sagemaker.model.ListAppsRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;

import java.util.stream.Collectors;

final class TranslatorForRequest {

    private TranslatorForRequest() {}

    /**
     * Translates ResourceModel input to an aws sdk create resource request.
     *
     * @param model resource model
     * @return aws sdk create resource request
     */
    static CreateAppRequest translateToCreateRequest(final ResourceModel model) {
        return CreateAppRequest.builder()
                .appName(model.getAppName())
                .appType(model.getAppType())
                .domainId(model.getDomainId())
                .resourceSpec(translateResourceSpec(model.getResourceSpec()))
                .userProfileName(model.getUserProfileName())
                .tags(Translator.streamOfOrEmpty(model.getTags())
                        .map(t -> Tag.builder()
                                .key(t.getKey())
                                .value(t.getValue())
                                .build())
                        .collect(Collectors.toList())
                ).build();
    }

    /**
     * Translates ResourceModel input to an aws sdk read resource request.
     *
     * @param model resource model
     * @return aws sdk read resource request
     */
    static DescribeAppRequest translateToReadRequest(final ResourceModel model) {
        return DescribeAppRequest.builder()
                .appName(model.getAppName())
                .appType(model.getAppType())
                .domainId(model.getDomainId())
                .userProfileName(model.getUserProfileName())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk delete resource request.
     *
     * @param model resource model
     * @return aws sdk delete resource request
     */
    static DeleteAppRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteAppRequest.builder()
                .appName(model.getAppName())
                .appType(model.getAppType())
                .domainId(model.getDomainId())
                .userProfileName(model.getUserProfileName())
                .build();
    }

    /**
     * Translates ResourceModel input to an aws sdk list resource request.
     *
     * @param nextToken token passed to the aws service describe resource request
     * @return list resource request
     */
    static ListAppsRequest translateToListRequest(final String nextToken) {
        return ListAppsRequest.builder().nextToken(nextToken).build();
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
}