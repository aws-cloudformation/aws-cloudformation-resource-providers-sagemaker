package software.amazon.sagemaker.imageversion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.sagemaker.model.CreateImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.ListImageVersionsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListImageVersionsResponse;

/**
 * This class is a centralized placeholder for the following.
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */
public class Translator {

    /**
     * Format a request to create an image version resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return createImageVersionRequest the aws service request to create an image version resource
     */
    static CreateImageVersionRequest translateToCreateRequest(final ResourceModel model, final String clientToken) {
        return CreateImageVersionRequest.builder()
            .imageName(model.getImageName())
            .baseImage(model.getBaseImage())
            .clientToken(clientToken)
            .build();
    }

    /**
     * Format a request to read an image version resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return describeImageVersionRequest the aws service request to read an image version resource
     */
    static DescribeImageVersionRequest translateToReadRequest(final ResourceModel model) {
        return DescribeImageVersionRequest.builder()
            .imageName(getImageNameFromVersionArn(model.getImageVersionArn()))
            .version(getImageVersionNumberFromVersionArn(model.getImageVersionArn()))
            .build();
    }

    /**
     * Format a request to delete an image version resource from the input CFN resource model.
     * @param model resource model passed into CFN handler
     * @return deleteImageVersionRequest the aws service request to delete an image version resource
     */
    static DeleteImageVersionRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteImageVersionRequest.builder()
            .imageName(getImageNameFromVersionArn(model.getImageVersionArn()))
            .version(getImageVersionNumberFromVersionArn(model.getImageVersionArn()))
            .build();
    }

    /**
     * Format a request to list all image version resources for an account.
     * @param model resource model passed into CFN handler
     * @param nextToken token passed to the aws service list resources request
     * @return listImageVersionsRequest the aws service request to list image version resources for an account
     */
    static ListImageVersionsRequest translateToListRequest(final ResourceModel model, final String nextToken) {
        return ListImageVersionsRequest.builder()
            .imageName(getImageNameFromVersionArn(model.getImageVersionArn()))
            .nextToken(nextToken)
            .build();
    }

    /**
     * Translates a list of ImageVersion resources from a list image version response into a list of CFN resource models.
     * @param listImageVersionsResponse the response from a list image version request
     * @return resourceModels list of CloudFormation resource models representing the list of image versions
     */
    static List<ResourceModel> translateFromListResponse(final ListImageVersionsResponse listImageVersionsResponse) {
        return streamOfOrEmpty(listImageVersionsResponse.imageVersions())
            .map(image -> ResourceModel.builder()
                    .imageVersionArn(image.imageVersionArn())
                    .build())
            .collect(Collectors.toList());
    }

    static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }

    /**
     * Translates image version resource from the service into a CloudFormation resource model.
     * @param describeImageVersionResponse the response from a an image version read request
     * @return resourceModel CloudFormation resource model representation of the image version
     */
    static ResourceModel translateFromReadResponse(final DescribeImageVersionResponse describeImageVersionResponse) {
        return ResourceModel.builder()
            .imageName(getImageNameFromVersionArn(describeImageVersionResponse.imageVersionArn()))
            .imageArn(describeImageVersionResponse.imageArn())
            .imageVersionArn(describeImageVersionResponse.imageVersionArn())
            .version(describeImageVersionResponse.version())
            .baseImage(describeImageVersionResponse.baseImage())
            .containerImage(describeImageVersionResponse.containerImage())
            .build();
    }

    private static String getImageNameFromVersionArn(final String imageVersionArn) {
        final String[] arnParts = StringUtils.split(imageVersionArn, '/');
        return arnParts[arnParts.length - 2];
    }

    private static Integer getImageVersionNumberFromVersionArn(final String imageVersionArn) {
        final String[] arnParts = StringUtils.split(imageVersionArn, '/');
        return Integer.parseInt(arnParts[arnParts.length - 1]);
    }
}
