package software.amazon.sagemaker.featuregroup;

import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.ListFeatureGroupsResponse;

import java.util.List;
import java.util.stream.Collectors;

public class TranslatorForResponse {

    private TranslatorForResponse() {}

    /**
     * Translates resource object from sdk into a resource model
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeFeatureGroupResponse awsResponse) {
        return ResourceModel.builder()
                .featureGroupName(awsResponse.featureGroupName())
                .recordIdentifierFeatureName(awsResponse.recordIdentifierFeatureName())
                .eventTimeFeatureName(awsResponse.eventTimeFeatureName())
                .featureDefinitions(translateToFeatureDefinitions(awsResponse.featureDefinitions()))
                .onlineStoreConfig(translateToOnlineStoreConfig(awsResponse.onlineStoreConfig()))
                .offlineStoreConfig(translateToOfflineStoreConfig(awsResponse.offlineStoreConfig()))
                .description(awsResponse.description())
                .roleArn(awsResponse.roleArn())
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListFeatureGroupsResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.featureGroupSummaries())
                .map(summary -> ResourceModel.builder()
                        .featureGroupName(summary.featureGroupName())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<FeatureDefinition> translateToFeatureDefinitions(
            List<software.amazon.awssdk.services.sagemaker.model.FeatureDefinition> origin) {
        return origin.stream()
                .map(f -> FeatureDefinition.builder()
                        .featureName(f.featureName())
                        .featureType(f.featureType().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private static OnlineStoreConfig translateToOnlineStoreConfig(
            software.amazon.awssdk.services.sagemaker.model.OnlineStoreConfig origin) {
        if (origin == null) {
            return null;
        }
        return OnlineStoreConfig.builder()
                .securityConfig(origin.securityConfig() == null ? null : OnlineStoreSecurityConfig.builder()
                        .kmsKeyId(origin.securityConfig().kmsKeyId())
                        .build())
                .enableOnlineStore(origin.enableOnlineStore())
                .build();
    }

    private static OfflineStoreConfig translateToOfflineStoreConfig(
            software.amazon.awssdk.services.sagemaker.model.OfflineStoreConfig origin) {
        if (origin == null) {
            return null;
        }
        return OfflineStoreConfig.builder()
                .disableGlueTableCreation(origin.disableGlueTableCreation())
                .dataCatalogConfig(origin.dataCatalogConfig() == null ? null :
                        DataCatalogConfig.builder()
                                .tableName(origin.dataCatalogConfig().tableName())
                                .catalog(origin.dataCatalogConfig().catalog())
                                .database(origin.dataCatalogConfig().database())
                                .build())
                .s3StorageConfig(origin.s3StorageConfig() == null ? null :
                        S3StorageConfig.builder()
                                .kmsKeyId(origin.s3StorageConfig().kmsKeyId())
                                .s3Uri(origin.s3StorageConfig().s3Uri())
                                .build())
                .build();
    }
}
