package software.amazon.sagemaker.pipeline;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains translation methods for object other than api request/response.
 * It also contains common methods required by other translators.
 */
public class Translator {

    /**
     * Throws Cfn exception corresponding to error code of the given exception.
     *
     * @param operation operation
     * @param resourceType resource type
     * @param resourceName resource name
     * @param e exception
     */
    static void throwCfnException(
            final String operation,
            final String resourceType,
            final String resourceName,
            final AwsServiceException e
    ) {

        if (e instanceof ResourceInUseException) {
            throw new ResourceAlreadyExistsException(resourceType, resourceName, e);
        }

        if (e instanceof ResourceNotFoundException) {
            throw new CfnNotFoundException(resourceType, resourceName, e);
        }

        if (e instanceof ResourceLimitExceededException) {
            throw new CfnServiceLimitExceededException(resourceType, e.getMessage(), e);
        }

        if(e.awsErrorDetails() != null && StringUtils.isNotBlank(e.awsErrorDetails().errorCode())) {
            String errorMessage = e.awsErrorDetails().errorMessage();
            switch (e.awsErrorDetails().errorCode()) {
                case "UnauthorizedOperation":
                    throw new CfnAccessDeniedException(errorMessage, e);
                case "ValidationException":
                    if (errorMessage.contains("names must be unique within an AWS account and region")) {
                        throw new CfnAlreadyExistsException(resourceType, resourceName, e);
                    }
                    throw new CfnInvalidRequestException(errorMessage, e);
                case "InternalError":
                case "ServiceUnavailable":
                    throw new CfnServiceInternalErrorException(errorMessage, e);
                case "ThrottlingException":
                    throw new CfnThrottlingException(errorMessage, e);
                default:
                    throw new CfnGeneralServiceException(errorMessage, e);
            }
        }

        throw new CfnGeneralServiceException(operation, e);
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
    static List<software.amazon.sagemaker.pipeline.Tag> sdkTagsToCfnTags(final List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        return tags.stream()
                .map(e -> software.amazon.sagemaker.pipeline.Tag.builder()
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
    static List<Tag> cfnTagsToSdkTags(final List<software.amazon.sagemaker.pipeline.Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }

        for (final software.amazon.sagemaker.pipeline.Tag tag : tags) {
            if (tag.getKey() == null) {
                throw new CfnInvalidRequestException("Tag cannot have a null key");
            }

            if (tag.getValue() == null) {
                throw new CfnInvalidRequestException(String.format("Tag cannot have a null value for key: %s", tag.getKey()));
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
     * Translates the CFN resource model to a list tags request on the target pipeline resource.
     * @param arn arn of the pipeline for which tags are being listed
     * @return listTagsRequest the aws service request to list tags on a pipeline resource
     */
    static ListTagsRequest translateToListTagsRequest(final String arn) {
        return ListTagsRequest.builder()
                .resourceArn(arn)
                .build();
    }

    /**
     * Construct add tags request from list of tags and pipeline arn.
     * @param tagsToAdd list of tags to be added to the pipeline
     * @param arn arn of the pipeline to which tags have to be added
     * @return addTagsRequest the aws service request to add tags on a pipeline resource
     */
    static AddTagsRequest translateToAddTagsRequest(final List<Tag> tagsToAdd, final String arn) {
        return AddTagsRequest.builder()
                .resourceArn(arn)
                .tags(tagsToAdd)
                .build();
    }

    /**
     * Construct delete tags request from list of tags and pipeline arn.
     * @param tagsToDelete list of tags to be deleted from the pipeline
     * @param arn arn of the pipeline from which tags have to be deleted
     * @return deleteTagsRequest the aws service request to delete tags from a pipeline resource
     */
    static DeleteTagsRequest translateToDeleteTagsRequest(final List<String> tagsToDelete, final String arn) {
        return DeleteTagsRequest.builder()
                .resourceArn(arn)
                .tagKeys(tagsToDelete)
                .build();
    }
}