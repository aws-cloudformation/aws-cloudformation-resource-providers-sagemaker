package software.amazon.sagemaker.project;

import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.ListProjectsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListProjectsResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ProjectStatus;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static CreateProjectRequest translateToCreateRequest(final ResourceModel model) {
        List<Tag> tags = cfnTagsToSdkTags(model.getTags());

        CreateProjectRequest.Builder builder = CreateProjectRequest.builder()
                .projectName(model.getProjectName())
                .projectDescription(model.getProjectDescription())
                .serviceCatalogProvisioningDetails(translate(model.getServiceCatalogProvisioningDetails()))
                .tags(tags);

        //optional fields
        Optional.ofNullable(model.getProjectDescription()).ifPresent(
                inp -> builder.projectDescription(inp)
        );

        return builder.build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeProjectRequest translateToReadRequest(final ResourceModel model) {

        return DescribeProjectRequest.builder()
                .projectName(model.getProjectName())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param describeProjectResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeProjectResponse describeProjectResponse) {

        return ResourceModel.builder()
                .projectArn(describeProjectResponse.projectArn())
                .projectName(describeProjectResponse.projectName())
                .projectStatus(describeProjectResponse.projectStatus().toString())
                .projectDescription(describeProjectResponse.projectDescription())
                .serviceCatalogProvisioningDetails(translate(describeProjectResponse.serviceCatalogProvisioningDetails()))
                .serviceCatalogProvisionedProductDetails(translate(describeProjectResponse.serviceCatalogProvisionedProductDetails()))
                .creationTime(describeProjectResponse.creationTime().toString())
                .build();
    }

    private static ServiceCatalogProvisionedProductDetails translate(
            software.amazon.awssdk.services.sagemaker.model.ServiceCatalogProvisionedProductDetails provisionedProductDetails) {
        return provisionedProductDetails != null ? ServiceCatalogProvisionedProductDetails.builder()
                .provisionedProductId(provisionedProductDetails.provisionedProductId())
                .provisionedProductStatusMessage(provisionedProductDetails.provisionedProductStatusMessage())
                .build() : null;
    }

    private static ServiceCatalogProvisioningDetails translate(
            software.amazon.awssdk.services.sagemaker.model.ServiceCatalogProvisioningDetails
                    provisioningDetails) {
        return provisioningDetails != null ? ServiceCatalogProvisioningDetails.builder()
                .pathId(provisioningDetails.pathId())
                .productId(provisioningDetails.productId())
                .provisioningArtifactId(provisioningDetails.provisioningArtifactId())
                .provisioningParameters(translateFrom(provisioningDetails.provisioningParameters()))
                .build() : null;
    }

    private static List<ProvisioningParameter> translateFrom(
            List<software.amazon.awssdk.services.sagemaker.model.ProvisioningParameter> provisioningParameters) {
        return provisioningParameters != null && isNotEmpty(provisioningParameters) ?
                provisioningParameters.stream()
                .map(e -> ProvisioningParameter.builder()
                        .key(e.key())
                        .value(e.value())
                        .build())
                .collect(Collectors.toList()) : null;
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteProjectRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteProjectRequest.builder()
                .projectName(model.getProjectName())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static ListProjectsRequest translateToListRequest(final String nextToken) {
        return ListProjectsRequest.builder()
                .nextToken(nextToken).build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListProjectsResponse awsResponse) {

        return Translator.streamOfOrEmpty(awsResponse.projectSummaryList())
                .map(summary -> ResourceModel.builder()
                        .creationTime(summary.creationTime().toString())
                        .projectArn(summary.projectArn())
                        .projectName(summary.projectName())
                        .projectStatus(summary.projectStatus().toString())
                        .projectDescription(summary.projectDescription())
                        .build())
                .filter(summary -> {
                    return false == summary.getProjectStatus().equals(ProjectStatus.DELETE_COMPLETED.toString());
                })
                .collect(Collectors.toList());

    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    /**
     * Converts input object to a Sagemaker SDK object.
     * @param input ServiceCatalogProvisioningDetails
     * @return Sagemaker ServiceCatalogProvisioningDetails object.
     */
    static software.amazon.awssdk.services.sagemaker.model.ServiceCatalogProvisioningDetails translate(
            final ServiceCatalogProvisioningDetails input) {
        return input == null ? null : software.amazon.awssdk.services.sagemaker.model.ServiceCatalogProvisioningDetails.builder()
                .pathId(input.getPathId())
                .productId(input.getProductId())
                .provisioningArtifactId(input.getProvisioningArtifactId())
                .provisioningParameters(translate(input.getProvisioningParameters()))
                .build();
    }

    /**
     * Converts input object to a Sagemaker SDK object.
     * @param input ServiceCatalogProvisioningDetails
     * @return Sagemaker ServiceCatalogProvisioningDetails object.
     */
    static List<software.amazon.awssdk.services.sagemaker.model.ProvisioningParameter> translate(
            final List<ProvisioningParameter> input) {
        return input != null && isNotEmpty(input) ? input.stream()
                .map(e -> software.amazon.awssdk.services.sagemaker.model.ProvisioningParameter.builder()
                        .key(e.getKey())
                        .value(e.getValue())
                        .build())
                .collect(Collectors.toList()) : null;
    }

    static List<Tag> cfnTagsToSdkTags(final List<software.amazon.sagemaker.project.Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }
        for (final software.amazon.sagemaker.project.Tag tag : tags) {
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
     * Translates the model to request to list tags of project
     * @param model resource model
     * @return awsRequest the aws service to list tags of project
     */
    static ListTagsRequest translateToListTagsRequest(final ResourceModel model) {
        return ListTagsRequest.builder()
                .resourceArn(model.getProjectArn())
                .build();
    }

    /**
     * Translates tag objects from resource model into list of tag objects of sdk
     * @param tags, sdk tags got from the aws service response
     * @return list of resource model tags
     */
    static List<software.amazon.sagemaker.project.Tag> sdkTagsToCfnTags(final List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        final List<software.amazon.sagemaker.project.Tag> cfnTags =
                tags.stream()
                        .map(e -> software.amazon.sagemaker.project.Tag.builder()
                                .key(e.key())
                                .value(e.value())
                                .build())
                        .collect(Collectors.toList());
        return cfnTags;
    }

    /**
     * Construct add tags request from list of tags and project arn
     * @param tagsToAdd, list of tags to be added to the project
     * @param arn, arn of the project to which tags have to be added.
     * @return awsRequest the aws service request to add tags to project
     */
    static AddTagsRequest translateToAddTagsRequest(final List<Tag> tagsToAdd, String arn) {
        return AddTagsRequest.builder()
                .resourceArn(arn)
                .tags(tagsToAdd)
                .build();
    }

    /**
     * Construct delete tags request from list of tags and project arn
     * @param tagsToDelete, list of tags to be deleted from the project
     * @param arn, arn of the project from which tags have to be deleted.
     * @return awsRequest the aws service request to add tags to project
     */
    static DeleteTagsRequest translateToDeleteTagsRequest(final List<String> tagsToDelete, String arn) {
        return DeleteTagsRequest.builder()
                .resourceArn(arn)
                .tagKeys(tagsToDelete)
                .build();
    }

    /**
     * Validate required fields does not differ
     * @param model resource model
     * @param response describe response
     */
    static boolean compareRequiredFields(final ResourceModel model, final DescribeProjectResponse response) {
        return model.getProjectDescription() == response.projectDescription()
            || model.getServiceCatalogProvisioningDetails().getProductId()
                == response.serviceCatalogProvisioningDetails().productId()
            || model.getServiceCatalogProvisioningDetails().getProvisioningArtifactId()
                == response.serviceCatalogProvisioningDetails().provisioningArtifactId();
    }
}
