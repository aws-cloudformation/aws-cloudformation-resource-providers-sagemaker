package software.amazon.sagemaker.app;

import software.amazon.awssdk.services.sagemaker.model.AppStatus;
import software.amazon.awssdk.services.sagemaker.model.DescribeAppResponse;
import software.amazon.awssdk.services.sagemaker.model.ListAppsResponse;
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
    static ResourceModel translateFromReadResponse(final DescribeAppResponse awsResponse) {
        return ResourceModel.builder()
                .appArn(awsResponse.appArn())
                .appName(awsResponse.appName())
                .appType(awsResponse.appTypeAsString())
                .domainId(awsResponse.domainId())
                .resourceSpec(translateResourceSpec(awsResponse.resourceSpec()))
                .userProfileName(awsResponse.userProfileName())
                .build();
    }

    /**
     * Translates the AWS SDK list response into a native resource model.
     *
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListAppsResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.apps())
                .filter(App -> !App.status().equals(AppStatus.DELETED) && !App.status().equals(AppStatus.FAILED))
                .map(App -> ResourceModel.builder()
                        .appName(App.appName())
                        .appType(App.appTypeAsString())
                        .domainId(App.domainId())
                        .userProfileName(App.userProfileName())
                        .build())
                .collect(Collectors.toList());
    }

    static software.amazon.sagemaker.app.ResourceSpec translateResourceSpec(
            ResourceSpec origin) {
        if (origin == null) {
            return null;
        }

        return software.amazon.sagemaker.app.ResourceSpec.builder()
                .instanceType(origin.instanceTypeAsString())
                .sageMakerImageArn(origin.sageMakerImageArn())
                .sageMakerImageVersionArn(origin.sageMakerImageVersionArn())
                .build();
    }
}
