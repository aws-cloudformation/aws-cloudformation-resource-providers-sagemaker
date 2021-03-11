package software.amazon.sagemaker.image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ListImagesRequest;
import software.amazon.awssdk.services.sagemaker.model.ListImagesResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.UpdateImageRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

/**
 * This class is a centralized placeholder for the following.
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */
public class Translator {
    private static final String DISPLAY_NAME = "DisplayName";
    private static final String DESCRIPTION = "Description";

    /**
     * Format a request to create an image resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return createImageRequest the aws service request to create an image resource
     */
    static CreateImageRequest translateToCreateRequest(final ResourceModel model) {
        final List<Tag> tags = cfnTagsToSdkTags(model.getTags());

        final CreateImageRequest.Builder createImageBuilder = CreateImageRequest.builder()
            .imageName(model.getImageName())
            .roleArn(model.getImageRoleArn())
            .tags(tags);

        //optional fields
        Optional.ofNullable(model.getImageDisplayName()).ifPresent(createImageBuilder::displayName);
        Optional.ofNullable(model.getImageDescription()).ifPresent(createImageBuilder::description);

        return createImageBuilder.build();
    }

    /**
     * Format a request to read an image resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return describeImageRequest the aws service request to read an image resource
     */
    static DescribeImageRequest translateToReadRequest(final ResourceModel model) {
        return DescribeImageRequest.builder()
            .imageName(model.getImageName())
            .build();
    }

    /**
     * Format a request to update an image resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return updateImageRequest the aws service request to update an image resource
     */
    static UpdateImageRequest translateToUpdateRequest(
        final ResourceModel model,
        final ResourceModel previousModel) {
        final UpdateImageRequest.Builder updateImageBuilder = UpdateImageRequest.builder()
            .imageName(model.getImageName());

        //optional fields
        Optional.ofNullable(model.getImageRoleArn()).ifPresent(updateImageBuilder::displayName);
        Optional.ofNullable(model.getImageDisplayName()).ifPresent(updateImageBuilder::displayName);
        Optional.ofNullable(model.getImageDescription()).ifPresent(updateImageBuilder::description);

        final List<String> deleteProperties = getPropertiesToDelete(model, previousModel);
        if (!deleteProperties.isEmpty()) {
            updateImageBuilder.deleteProperties(deleteProperties);
        }

        return updateImageBuilder.build();
    }

    /**
     * Returns a list of properties to be deleted on an image by comparing the existing and requested resource models.
     * @param newModel the desired state resource model
     * @param previousModel the existing state resource model
     * @return deleteProperties list of attribute names (as strings) that should be removed from the image resource
     */
    static List<String> getPropertiesToDelete(final ResourceModel newModel, final ResourceModel previousModel) {
        final List<String> deleteProperties = new ArrayList<>();

        if (previousModel.getImageDisplayName() != null  && newModel.getImageDisplayName() == null) {
            deleteProperties.add(DISPLAY_NAME);
        }

        if (previousModel.getImageDescription() != null  && newModel.getImageDescription() == null) {
            deleteProperties.add(DESCRIPTION);
        }

        return deleteProperties;
    }

    /**
     * Format a request to delete an image resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return deleteImageRequest the aws service request to delete an image resource
     */
    static DeleteImageRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteImageRequest.builder()
            .imageName(model.getImageName())
            .build();
    }

    /**
     * Format a request to list all image resources for an account.
     * @param nextToken token passed to the aws service list resources request
     * @return listImagesRequest the aws service request to list image resources for an account
     */
    static ListImagesRequest translateToListRequest(final String nextToken) {
        return ListImagesRequest.builder()
            .nextToken(nextToken)
            .build();
    }

    /**
     * Translates the CFN resource model to a list tags request on the target image resource.
     * @param model resource model passed into CFN handler
     * @return listTagsRequest the aws service request to list tags on an image resource
     */
    static ListTagsRequest translateToListTagsRequest(final ResourceModel model) {
        return ListTagsRequest.builder()
            .resourceArn(model.getImageArn())
            .build();
    }

    /**
     * Construct add tags request from list of tags and image arn.
     * @param tagsToAdd list of tags to be added to the image
     * @param arn arn of the image to which tags have to be added
     * @return addTagsRequest the aws service request to add tags on an image resource
     */
    static AddTagsRequest translateToAddTagsRequest(final List<Tag> tagsToAdd, final String arn) {
        return AddTagsRequest.builder()
            .resourceArn(arn)
            .tags(tagsToAdd)
            .build();
    }

    /**
     * Construct delete tags request from list of tags and image arn.
     * @param tagsToDelete list of tags to be deleted from the image
     * @param arn arn of the image from which tags have to be deleted
     * @return deleteTagsRequest the aws service request to delete tags from an image resource
     */
    static DeleteTagsRequest translateToDeleteTagsRequest(final List<String> tagsToDelete, final String arn) {
        return DeleteTagsRequest.builder()
            .resourceArn(arn)
            .tagKeys(tagsToDelete)
            .build();
    }

    /**
     * Translates image resource from the service into a CloudFormation resource model.
     * @param describeImageResponse the response from a an image read request
     * @return resourceModel CloudFormation resource model representation of the image
     */
    static ResourceModel translateFromReadResponse(final DescribeImageResponse describeImageResponse) {
        return ResourceModel.builder()
            .imageArn(describeImageResponse.imageArn())
            .imageName(describeImageResponse.imageName())
            .imageDisplayName(describeImageResponse.displayName())
            .imageDescription(describeImageResponse.description())
            .imageRoleArn(describeImageResponse.roleArn())
            .build();
    }

    /**
     * Translates a list of Image resources from a list image response into a list of CFN resource models.
     * @param listImagesResponse the response from a list image request
     * @return resourceModels list of CloudFormation resource models representing the list of images
     */
    static List<ResourceModel> translateFromListResponse(final ListImagesResponse listImagesResponse) {
        return streamOfOrEmpty(listImagesResponse.images())
            .map(image -> ResourceModel.builder()
                    .imageArn(image.imageArn())
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
    static List<software.amazon.sagemaker.image.Tag> sdkTagsToCfnTags(final List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        return tags.stream()
            .map(e -> software.amazon.sagemaker.image.Tag.builder()
                    .key(e.key())
                    .value(e.value())
                    .build())
            .collect(Collectors.toList());
    }

    /**
     * Converts the provided list of CFN Tag objects into the equivalent SageMaker Tag representation.
     * @param tags list of CloudFormation Tag objects
     * @return sdkTags list of SageMaker tags
     */
    static List<Tag> cfnTagsToSdkTags(final List<software.amazon.sagemaker.image.Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }

        for (final software.amazon.sagemaker.image.Tag tag : tags) {
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
}
