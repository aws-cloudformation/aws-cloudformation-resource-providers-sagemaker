package software.amazon.sagemaker.modelpackagegroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.PutModelPackageGroupPolicyRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageGroupPolicyRequest;
import software.amazon.awssdk.services.sagemaker.model.GetModelPackageGroupPolicyRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackageGroupsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListModelPackageGroupsResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Translates the model to Request to create a model package group
     * @param model resource model
     * @return awsRequest the aws service request to create a model package group
     */
    static CreateModelPackageGroupRequest translateToCreateRequest(final ResourceModel model) {
        List<Tag> tags = cfnTagsToSdkTags(model.getTags());
        CreateModelPackageGroupRequest request;
        if (!tags.isEmpty()) {
            request = CreateModelPackageGroupRequest.builder()
                    .modelPackageGroupName(model.getModelPackageGroupName())
                    .modelPackageGroupDescription(model.getModelPackageGroupDescription())
                    .tags(tags)
                    .build();
        }
        else {
            request = CreateModelPackageGroupRequest.builder()
                    .modelPackageGroupName(model.getModelPackageGroupName())
                    .modelPackageGroupDescription(model.getModelPackageGroupDescription())
                    .build();
        }
        return request;
    }


    /**
     * Translates the model to Request to put a model package group policy
     * @param model resource model
     * @return awsRequest the aws service request to put a model package group policy
     */
    static PutModelPackageGroupPolicyRequest translateToPutModelPackageGroupPolicyRequest(final ResourceModel model) {
        PutModelPackageGroupPolicyRequest putModelPackageGroupPolicyRequest;
        try {
            String policy = MAPPER.writeValueAsString(model.getModelPackageGroupPolicy());
            putModelPackageGroupPolicyRequest = PutModelPackageGroupPolicyRequest.builder()
                    .modelPackageGroupName(model.getModelPackageGroupName())
                    .resourcePolicy(policy)
                    .build();
        } catch (JsonProcessingException e) {
            throw new CfnInvalidRequestException(e);
        }
        return putModelPackageGroupPolicyRequest;
    }

    /**
     * Translates the model to Request to delete a model package group policy
     * @param model resource model
     * @return awsRequest the aws service request to delete a model package group policy
     */
    static DeleteModelPackageGroupPolicyRequest translateToDeleteModelPackageGroupPolicyRequest(final ResourceModel model) {
        return DeleteModelPackageGroupPolicyRequest.builder()
                .modelPackageGroupName(model.getModelPackageGroupName())
                .build();
    }

    /**
     * Translates the model to Request to get a model package group policy
     * @param model resource model
     * @return awsRequest the aws service request to get a model package group policy
     */
    static GetModelPackageGroupPolicyRequest translateToGetModelPackageGroupPolicyRequest(final ResourceModel model) {
        return GetModelPackageGroupPolicyRequest.builder()
                .modelPackageGroupName(model.getModelPackageGroupName())
                .build();
    }

    /**
     * Translates the model to Request to read a model package group
     * @param model resource model
     * @return awsRequest the aws service request to describe a model package group
     */
    static DescribeModelPackageGroupRequest translateToReadRequest(final ResourceModel model) {
        return DescribeModelPackageGroupRequest.builder()
                .modelPackageGroupName(model.getModelPackageGroupName()).build();
    }

    /**
     * Translates resource object from sdk into a resource model
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeModelPackageGroupResponse awsResponse) {
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L58-L73
        return ResourceModel.builder()
                .creationTime(awsResponse.creationTime().toString())
                .modelPackageGroupArn(awsResponse.modelPackageGroupArn())
                .modelPackageGroupName(awsResponse.modelPackageGroupName())
                .modelPackageGroupDescription(awsResponse.modelPackageGroupDescription())
                .modelPackageGroupStatus(awsResponse.modelPackageGroupStatus().toString())
                .build();
    }

    /**
     * Translates the model to Request to delete a model package group
     * @param model resource model
     * @return awsRequest the aws service request to delete a model package group
     */
    static DeleteModelPackageGroupRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteModelPackageGroupRequest.builder()
                .modelPackageGroupName(model.getModelPackageGroupArn() != null
                        ? model.getModelPackageGroupArn() : model.getModelPackageGroupName())
                .build();
    }

    /**
     * Translates next token to Request to list model package groups
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list model package groups within aws account
     */
    static ListModelPackageGroupsRequest translateToListRequest(final String nextToken) {
        return ListModelPackageGroupsRequest.builder()
                .nextToken(nextToken).build();
    }

    /**
     * Translates resource objects from sdk into list of resource models
     * @param awsResponse the aws service list resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListModelPackageGroupsResponse awsResponse) {
        return Translator.streamOfOrEmpty(awsResponse.modelPackageGroupSummaryList())
                .map(summary -> ResourceModel.builder()
                        .creationTime(summary.creationTime().toString())
                        .modelPackageGroupName(summary.modelPackageGroupName())
                        .modelPackageGroupArn(summary.modelPackageGroupArn())
                        .modelPackageGroupDescription(summary.modelPackageGroupDescription())
                        .modelPackageGroupStatus(summary.modelPackageGroupStatus().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    /**
     * Translates tag objects from sdk into list of tag objects in resource model
     * @param tags, resource model tags
     * @return list of sdk tags
     */
    static List<Tag> cfnTagsToSdkTags(final List<software.amazon.sagemaker.modelpackagegroup.Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        for (final software.amazon.sagemaker.modelpackagegroup.Tag tag : tags) {
            if (tag.getKey() == null) {
                throw new CfnInvalidRequestException("Tags cannot have a null key");
            }
            if (tag.getValue() == null) {
                throw new CfnInvalidRequestException("Tags cannot have a null value");
            }
        }
        return tags.stream()
                .map(e -> Tag.builder()
                        .key(e.getKey())
                        .value(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Translates tag objects from resource model into list of tag objects of sdk
     * @param tags, sdk tags got from the aws service response
     * @return list of resource model tags
     */
    static List<software.amazon.sagemaker.modelpackagegroup.Tag> sdkTagsToCfnTags(final List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        final List<software.amazon.sagemaker.modelpackagegroup.Tag> cfnTags =
                tags.stream()
                        .map(e -> software.amazon.sagemaker.modelpackagegroup.Tag.builder()
                                .key(e.key())
                                .value(e.value())
                                .build())
                        .collect(Collectors.toList());
        return cfnTags;
    }

    /**
     * Translates the model to request to list tags of model package group
     * @param model resource model
     * @return awsRequest the aws service to list tags of model package group
     */
    static ListTagsRequest translateToListTagsRequest(final ResourceModel model) {
        return ListTagsRequest.builder()
                .resourceArn(model.getModelPackageGroupArn())
                .build();
    }

    /**
     * Construct add tags request from list of tags and model package group arn
     * @param tagsToAdd, list of tags to be added to the model package group
     * @param arn, arn of the model package group to which tags have to be added.
     * @return awsRequest the aws service request to add tags to model package group
     */
    static AddTagsRequest translateToAddTagsRequest(final List<Tag> tagsToAdd, String arn) {
        return AddTagsRequest.builder()
                .resourceArn(arn)
                .tags(tagsToAdd)
                .build();
    }

    /**
     * Construct delete tags request from list of tags and model package group arn
     * @param tagsToDelete, list of tags to be deleted from the model package group
     * @param arn, arn of the model package group from which tags have to be deleted.
     * @return awsRequest the aws service request to add tags to model package group
     */
    static DeleteTagsRequest translateToDeleteTagsRequest(final List<String> tagsToDelete, String arn) {
        return DeleteTagsRequest.builder()
                .resourceArn(arn)
                .tagKeys(tagsToDelete)
                .build();
    }
}
