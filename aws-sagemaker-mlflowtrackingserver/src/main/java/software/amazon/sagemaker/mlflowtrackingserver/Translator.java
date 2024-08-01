package software.amazon.sagemaker.mlflowtrackingserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeMlflowTrackingServerResponse;
import software.amazon.awssdk.services.sagemaker.model.ListMlflowTrackingServersRequest;
import software.amazon.awssdk.services.sagemaker.model.ListMlflowTrackingServersResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateMlflowTrackingServerRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

/**
 * This class is a centralized placeholder for the following.
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */
public class Translator {

    /**
     * Format a request to create an mlflow tracking server resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return createMlflowTrackingServerRequest the aws service request to create an mlflow tracking server resource
     */
    static CreateMlflowTrackingServerRequest translateToCreateRequest(final ResourceModel model) {
        return CreateMlflowTrackingServerRequest.builder()
            .artifactStoreUri(model.getArtifactStoreUri())
            .automaticModelRegistration(model.getAutomaticModelRegistration())
            .trackingServerName(model.getTrackingServerName())
            .mlflowVersion(model.getMlflowVersion())
            .roleArn(model.getRoleArn())
            .trackingServerSize(model.getTrackingServerSize())
            .weeklyMaintenanceWindowStart(model.getWeeklyMaintenanceWindowStart())
            .build();
    }

    /**
     * Format a request to read an mlflow tracking server resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return describeMlflowTrackingServerRequest the aws service request to read an mlflow tracking server resource
     */
    static DescribeMlflowTrackingServerRequest translateToReadRequest(final ResourceModel model) {
        return DescribeMlflowTrackingServerRequest.builder()
            .trackingServerName(model.getTrackingServerName())
            .build();
    }

    /**
     * Format a request to update an mlflow tracking server resource from the input CFN resource model.
     *
     * @param model resource model passed into CFN handler
     * @return updateMlflowTrackingServerRequest the aws service request to update an mlflow tracking server resource
     */
    static UpdateMlflowTrackingServerRequest translateToUpdateRequest(final ResourceModel model, final ResourceModel previousModel) {

        if (!ImmutabilityHelper.isChangeMutable(previousModel, model)) {
            throw new CfnInvalidRequestException("Resource update request is invalid");
        }
        final UpdateMlflowTrackingServerRequest.Builder updateBuilder =
                UpdateMlflowTrackingServerRequest.builder();

        updateBuilder.artifactStoreUri(model.getArtifactStoreUri() == null
                ? previousModel.getArtifactStoreUri()
                : model.getArtifactStoreUri());
        updateBuilder.automaticModelRegistration(model.getAutomaticModelRegistration() == null
                ? previousModel.getAutomaticModelRegistration()
                : model.getAutomaticModelRegistration());
        updateBuilder.trackingServerName(model.getTrackingServerName());
        updateBuilder.trackingServerSize(model.getTrackingServerSize() == null
                ? previousModel.getTrackingServerSize()
                : model.getTrackingServerSize());
        updateBuilder.weeklyMaintenanceWindowStart(model.getWeeklyMaintenanceWindowStart() == null
                ? previousModel.getWeeklyMaintenanceWindowStart()
                : model.getWeeklyMaintenanceWindowStart());

        return updateBuilder.build();
    }

    /**
     * Format a request to delete an mlflow tracking server resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return deleteMlflowTrackingServerRequest the aws service request to delete an mlflow tracking server resource
     */
    static DeleteMlflowTrackingServerRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteMlflowTrackingServerRequest.builder()
            .trackingServerName(model.getTrackingServerName())
            .build();
    }

    /**
     * Format a request to list all mlflow tracking server resources for an account.
     * @param nextToken token passed to the aws service list resources request
     * @return listMlflowTrackingServersRequest the aws service request to list mlflow tracking server resources for an account
     */
    static ListMlflowTrackingServersRequest translateToListRequest(final String nextToken) {
        return ListMlflowTrackingServersRequest.builder()
            .nextToken(nextToken)
            .build();
    }

    /**
     * Translates the CFN resource model to a list tags request on the target mlflow tracking server resource.
     * @param model resource model passed into CFN handler
     * @return listTagsRequest the aws service request to list tags on an mlflow tracking server resource
     */
    static ListTagsRequest translateToListTagsRequest(final ResourceModel model) {
        return ListTagsRequest.builder()
            .resourceArn(model.getTrackingServerArn())
            .build();
    }

    /**
     * Construct add tags request from list of tags and mlflow tracking server arn.
     * @param tagsToAdd list of tags to be added to the mlflow tracking server
     * @param arn arn of the mlflow tracking server to which tags have to be added
     * @return addTagsRequest the aws service request to add tags on an mlflow tracking server resource
     */
    static AddTagsRequest translateToAddTagsRequest(
            final Collection<software.amazon.awssdk.services.sagemaker.model.Tag> tagsToAdd, final String arn) {
        return AddTagsRequest.builder()
            .resourceArn(arn)
            .tags(tagsToAdd)
            .build();
    }

    /**
     * Construct delete tags request from list of tags and mlflow tracking server arn.
     * @param tagsToDelete list of tags to be deleted from the mlflow tracking server
     * @param arn arn of the mlflow tracking server from which tags have to be deleted
     * @return deleteTagsRequest the aws service request to delete tags from an mlflow tracking server resource
     */
    static DeleteTagsRequest translateToDeleteTagsRequest(
            final Collection<software.amazon.awssdk.services.sagemaker.model.Tag> tagsToDelete, final String arn) {
        final Set<String> tagKeysToRemove = tagsToDelete.stream()
                .map(software.amazon.awssdk.services.sagemaker.model.Tag::key)
                .collect(Collectors.toSet());
        return DeleteTagsRequest.builder()
            .resourceArn(arn)
            .tagKeys(tagKeysToRemove)
            .build();
    }

    /**
     * Translates mlflow tracking server resource from the service into a CloudFormation resource model.
     * @param describeMlflowTrackingServerResponse the response from an mlflow tracking server read request
     * @return resourceModel CloudFormation resource model representation of the mlflow tracking server
     */
    static ResourceModel translateFromReadResponse(final DescribeMlflowTrackingServerResponse describeMlflowTrackingServerResponse) {
        return ResourceModel.builder()
            .artifactStoreUri(describeMlflowTrackingServerResponse.artifactStoreUri())
            .automaticModelRegistration(describeMlflowTrackingServerResponse.automaticModelRegistration())
            .mlflowVersion(describeMlflowTrackingServerResponse.mlflowVersion())
            .trackingServerName(describeMlflowTrackingServerResponse.trackingServerName())
            .roleArn(describeMlflowTrackingServerResponse.roleArn())
            .trackingServerArn(describeMlflowTrackingServerResponse.trackingServerArn())
            .trackingServerSize(describeMlflowTrackingServerResponse.trackingServerSizeAsString())
            .weeklyMaintenanceWindowStart(describeMlflowTrackingServerResponse.weeklyMaintenanceWindowStart())
            .build();
    }

    /**
     * Translates a list of mlflow tracking server resources from a list mlflow tracking server response into a list of CFN resource models.
     * @param listMlflowTrackingServersResponse the response from a list mlflow tracking server request
     * @return resourceModels list of CloudFormation resource models representing the list of mlflow tracking servers
     */
    static List<ResourceModel> translateFromListResponse(final ListMlflowTrackingServersResponse listMlflowTrackingServersResponse) {
        return streamOfOrEmpty(listMlflowTrackingServersResponse.trackingServerSummaries())
            .map(trackingServerSummary -> ResourceModel.builder()
                    .trackingServerArn(trackingServerSummary.trackingServerArn())
                    .trackingServerName(trackingServerSummary.trackingServerName())
                    .build())
            .collect(Collectors.toList());
    }

    static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }

    /**
     * Converts the provided list of SageMaker Tag objects into the equivalent CFN Tag representation.
     * @param tags list of SageMaker tags
     * @return cfnTags list of CloudFormation Tag objects
     */
    static List<software.amazon.sagemaker.mlflowtrackingserver.Tag> sdkTagsToCfnTags(
            final List<software.amazon.awssdk.services.sagemaker.model.Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        return tags.stream()
            .map(sdkTag -> software.amazon.sagemaker.mlflowtrackingserver.Tag.builder()
                    .key(sdkTag.key())
                    .value(sdkTag.value())
                    .build())
            .collect(Collectors.toList());
    }

    /**
     * Converts the provided list of CFN Tag objects into the equivalent SageMaker Tag representation.
     * @param tags list of CloudFormation Tag objects
     * @return sdkTags list of SageMaker tags
     */
    static List<software.amazon.awssdk.services.sagemaker.model.Tag> cfnTagsToSdkTags(
            final List<software.amazon.sagemaker.mlflowtrackingserver.Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }

        for (final software.amazon.sagemaker.mlflowtrackingserver.Tag tag : tags) {
            if (tag.getKey() == null) {
                throw new CfnInvalidRequestException("Tags cannot have a null key");
            }

            if (tag.getValue() == null) {
                throw new CfnInvalidRequestException("Tags cannot have a null value");
            }
        }

        return tags.stream()
            .map(cfnTag -> Tag.builder()
                    .key(cfnTag.getKey())
                    .value(cfnTag.getValue())
                    .build())
            .collect(Collectors.toList());
    }
}
