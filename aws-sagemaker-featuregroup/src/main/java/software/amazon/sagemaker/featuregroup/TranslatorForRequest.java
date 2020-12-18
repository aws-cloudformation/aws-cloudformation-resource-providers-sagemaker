package software.amazon.sagemaker.featuregroup;

import software.amazon.awssdk.services.sagemaker.model.CreateFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DataCatalogConfig;
import software.amazon.awssdk.services.sagemaker.model.DeleteFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.FeatureDefinition;
import software.amazon.awssdk.services.sagemaker.model.ListFeatureGroupsRequest;
import software.amazon.awssdk.services.sagemaker.model.OfflineStoreConfig;
import software.amazon.awssdk.services.sagemaker.model.OnlineStoreConfig;
import software.amazon.awssdk.services.sagemaker.model.OnlineStoreSecurityConfig;
import software.amazon.awssdk.services.sagemaker.model.S3StorageConfig;
import software.amazon.awssdk.services.sagemaker.model.Tag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for handlers like read/list
 */
final class TranslatorForRequest {

	private TranslatorForRequest() {}

	/**
     * Request to create a resource
     * @param model resource model
     * @return createFeatureGroupRequest - service request to create a resource
     */
    static CreateFeatureGroupRequest translateToCreateRequest(final ResourceModel model) {
        return CreateFeatureGroupRequest.builder()
                .featureGroupName(model.getFeatureGroupName())
                .recordIdentifierFeatureName(model.getRecordIdentifierFeatureName())
                .eventTimeFeatureName(model.getEventTimeFeatureName())
                .featureDefinitions(translateFeatureDefinitions(model.getFeatureDefinitions()))
                .onlineStoreConfig(translateOnlineStoreConfig(model.getOnlineStoreConfig()))
                .offlineStoreConfig(translateOfflineStoreConfig(model.getOfflineStoreConfig()))
                .description(model.getDescription())
                .roleArn(model.getRoleArn())
                .tags(Translator.streamOfOrEmpty(model.getTags())
                        .map(t -> Tag.builder()
                                .key(t.getKey())
                                .value(t.getValue())
                                .build())
                        .collect(Collectors.toList())
                ).build();
    }

    /**
     * Request to read a resource
     * @param model resource model
     * @return describeFeatureGroupRequest - the aws service request to describe a resource
     */
    static DescribeFeatureGroupRequest translateToReadRequest(final ResourceModel model) {
        return DescribeFeatureGroupRequest.builder()
                .featureGroupName(model.getFeatureGroupName())
                .build();
    }

    /**
     * Request to delete a resource
     * @param model resource model
     * @return deleteFeatureGroupRequest - the aws service request to delete a resource
     */
    static DeleteFeatureGroupRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteFeatureGroupRequest.builder()
                .featureGroupName(model.getFeatureGroupName())
                .build();
    }

    /**
     * Request to list properties of a previously created resource
     * @param nextToken token passed to the aws service describe resource request
     * @return awsRequest the aws service request to describe resources within aws account
     */
    static ListFeatureGroupsRequest translateToListRequest(final String nextToken) {
        return ListFeatureGroupsRequest.builder().nextToken(nextToken).build();
    }

    private static List<FeatureDefinition> translateFeatureDefinitions(
            List<software.amazon.sagemaker.featuregroup.FeatureDefinition> origin) {
        if (origin == null) {
            return null;
        }
        return origin.stream()
                .map(f -> FeatureDefinition.builder()
                        .featureName(f.getFeatureName())
                        .featureType(f.getFeatureType())
                        .build())
                .collect(Collectors.toList());
    }

    private static OnlineStoreConfig translateOnlineStoreConfig(
            software.amazon.sagemaker.featuregroup.OnlineStoreConfig origin) {
        if (origin == null) {
            return null;
        }
        return OnlineStoreConfig.builder()
                .securityConfig(origin.getSecurityConfig() == null ? null : OnlineStoreSecurityConfig.builder()
                        .kmsKeyId(origin.getSecurityConfig().getKmsKeyId())
                        .build())
                .enableOnlineStore(origin.getEnableOnlineStore())
                .build();
    }

    private static OfflineStoreConfig translateOfflineStoreConfig(
            software.amazon.sagemaker.featuregroup.OfflineStoreConfig origin) {
        if (origin == null) {
            return null;
        }
        return OfflineStoreConfig.builder()
                .disableGlueTableCreation(origin.getDisableGlueTableCreation())
                .dataCatalogConfig(origin.getDataCatalogConfig() == null ? null :
                        DataCatalogConfig.builder()
                                .tableName(origin.getDataCatalogConfig().getTableName())
                                .catalog(origin.getDataCatalogConfig().getCatalog())
                                .database(origin.getDataCatalogConfig().getDatabase())
                                .build())
                .s3StorageConfig(origin.getS3StorageConfig() == null ? null :
                        S3StorageConfig.builder()
                                .kmsKeyId(origin.getS3StorageConfig().getKmsKeyId())
                                .s3Uri(origin.getS3StorageConfig().getS3Uri())
                                .build())
                .build();
    }
}