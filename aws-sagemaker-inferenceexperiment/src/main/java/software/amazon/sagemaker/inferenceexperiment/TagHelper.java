package software.amazon.sagemaker.inferenceexperiment;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagHelper {

    /**
     * Handles updating tags on the Image resource if changes are present.
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param desiredTags the list of desired tags
     * @param callbackContext the callback context
     * @return progressEvent, in progress with delay callback and model state
     */
    public static ProgressEvent<ResourceModel, CallbackContext> updateResourceTags(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final List<software.amazon.sagemaker.inferenceexperiment.Tag> desiredTags,
            final CallbackContext callbackContext) {
        try {
            processTagsDelta(proxyClient, model, desiredTags);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(String.format("Failure reason: %s", e.getMessage()), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Client invocation of the list tags request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param request the aws service request to list tags on a resource
     * @param proxyClient the aws service client to make the call
     * @return list tags resource response
     */
    public static ListTagsResponse listResourceTags(
            final ListTagsRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        ListTagsResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::listTags);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.resourceArn(), e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * Consolidate resource tags
     * @param model the resource model
     * @param request the handler request
     * @return list of tags
     */
    public static List<software.amazon.sagemaker.inferenceexperiment.Tag> consolidateResourceTags(
            final ResourceModel model,
            final ResourceHandlerRequest<ResourceModel> request) {

        Map<String, String> consolidateTags = new HashMap<>();

        if (!CollectionUtils.isNullOrEmpty(model.getTags())) {
            for (software.amazon.sagemaker.inferenceexperiment.Tag tag : model.getTags()) {
                consolidateTags.put(tag.getKey(), tag.getValue());
            }
        }
        if (!CollectionUtils.isNullOrEmpty(request.getDesiredResourceTags())) {
            consolidateTags.putAll(request.getDesiredResourceTags());
        }
        // Disable system tags
        // if (!CollectionUtils.isNullOrEmpty(request.getSystemTags())) {
        //    consolidateTags.putAll(request.getSystemTags());
        // }

        return consolidateTags.keySet().stream()
                .map(tagKey -> software.amazon.sagemaker.inferenceexperiment.Tag.builder()
                        .key(tagKey)
                        .value(consolidateTags.get(tagKey))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Identify the tag difference between existing and desired resource state. Add or delete tags on the resource.
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param desiredTags the list of desired tags
     */
    private static void processTagsDelta(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final List<software.amazon.sagemaker.inferenceexperiment.Tag> desiredTags) {

        final List<Tag> newTags =
                CollectionUtils.isNullOrEmpty(desiredTags) ? new ArrayList<>() : desiredTags.stream()
                        .map(TranslatorForRequest::translate)
                        .collect(Collectors.toList());

        final ListTagsResponse response = listResourceTags(
                TranslatorForRequest.translateToListTagsRequest(model.getArn()), proxyClient);
        final List<Tag> existingTags =
                CollectionUtils.isNullOrEmpty(response.tags()) ? new ArrayList<>() : response.tags();

        final List<software.amazon.awssdk.services.sagemaker.model.Tag> tagsToAdd = newTags.stream()
                .filter(tag -> !existingTags.contains(tag))
                .collect(Collectors.toList());
        final List<String> tagsToAddKeys = tagsToAdd.stream()
                .map(software.amazon.awssdk.services.sagemaker.model.Tag::key)
                .collect(Collectors.toList());
        final List<Tag> tagsToRemove = existingTags.stream()
                .filter(tag -> !newTags.contains(tag) && !tagsToAddKeys.contains(tag.key()))
                .collect(Collectors.toList());

        if (!tagsToRemove.isEmpty()) {
            proxyClient.injectCredentialsAndInvokeV2(
                    TranslatorForRequest.translateToDeleteTagsRequest(tagsToRemove, model.getArn()),
                    proxyClient.client()::deleteTags);
        }
        if (!tagsToAdd.isEmpty()) {
            proxyClient.injectCredentialsAndInvokeV2(
                    TranslatorForRequest.translateToAddTagsRequest(tagsToAdd, model.getArn()),
                    proxyClient.client()::addTags);
        }
    }
}
