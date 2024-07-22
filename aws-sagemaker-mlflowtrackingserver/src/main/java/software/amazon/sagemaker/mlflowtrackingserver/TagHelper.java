package software.amazon.sagemaker.mlflowtrackingserver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class TagHelper {
    /**
     * convertToMap
     *
     * Converts a collection of Tag objects to a tag-name -> tag-value map.
     *
     * Note: Tag objects with null tag values will not be included in the output
     * map.
     *
     * @param tags Collection of tags to convert
     * @return Converted Map of tags
     */
    public static Map<String, String> convertToMap(final Collection<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyMap();
        }
        return tags.stream()
                .filter(tag -> tag.value() != null)
                .collect(Collectors.toMap(
                        Tag::key,
                        Tag::value,
                        (oldValue, newValue) -> newValue));
    }

    /**
     * convertToSet
     *
     * Converts a tag map to a set of Tag objects.
     *
     * Note: Like convertToMap, convertToSet filters out value-less tag entries.
     *
     * @param tagMap Map of tags to convert
     * @return Set of Tag objects
     */
    public static Set<Tag> convertToSet(final Map<String, String> tagMap) {
        if (MapUtils.isEmpty(tagMap)) {
            return Collections.emptySet();
        }
        return tagMap.entrySet().stream()
                .filter(mapEntry -> mapEntry.getValue() != null)
                .map(mapEntry -> Tag.builder()
                        .key(mapEntry.getKey())
                        .value(mapEntry.getValue())
                        .build())
                .collect(Collectors.toSet());
    }

    /**
     * shouldUpdateTags
     *
     * Determines whether user defined tags have been changed during update.
     */
    public static boolean shouldUpdateTags(
            final ResourceHandlerRequest<ResourceModel> handlerRequest,
            final Logger logger
    ) {
        final Map<String, String> previousTags = getPreviouslyAttachedTags(handlerRequest, logger);
        final Map<String, String> desiredTags = getNewDesiredTags(handlerRequest, logger);
        return ObjectUtils.notEqual(previousTags, desiredTags);
    }

    /**
     * getPreviouslyAttachedTags
     *
     * If stack tags and resource tags are not merged together in Configuration class, we will get previously
     * attached tags from
     * handlerRequest.getPreviousResourceTags() (stack tags),
     * handlerRequest.getPreviousResourceState().getTags() (resource tags).
     */
    public static Map<String, String> getPreviouslyAttachedTags(
            final ResourceHandlerRequest<ResourceModel> handlerRequest,
            final Logger logger
    ) {
        final Map<String, String> previousTags = new HashMap<>();

        // get previous stack level tags from handlerRequest
        if (handlerRequest.getPreviousResourceTags() != null) {
            previousTags.putAll(handlerRequest.getPreviousResourceTags());
            logger.log(String.format("Previous tags after adding resource tags: %s", previousTags));
        }

        // get resource level tags from previous resource state based on your tag property name
        if (handlerRequest.getPreviousResourceState() != null && handlerRequest.getPreviousResourceState().getTags() != null) {
            final List<Tag> tags = Translator.cfnTagsToSdkTags(handlerRequest.getPreviousResourceState().getTags());
            previousTags.putAll(convertToMap(tags));
            logger.log(String.format("Previous tags after adding resource state tags: %s", previousTags));
        }

        return previousTags;
    }

    /**
     * getNewDesiredTags
     *
     * If stack tags and resource tags are not merged together in Configuration class, we will get new desired tags from
     * handlerRequest.getDesiredResourceTags() (stack tags),
     * handlerRequest.getDesiredResourceState().getTags() (resource tags).
     */
    public static Map<String, String> getNewDesiredTags(final ResourceHandlerRequest<ResourceModel> handlerRequest,
                                                 final Logger logger) {
        final Map<String, String> desiredTags = new HashMap<>();

        // get desired stack level tags from handlerRequest
        if (handlerRequest.getDesiredResourceTags() != null) {
            desiredTags.putAll(handlerRequest.getDesiredResourceTags());
            logger.log(String.format("Desired tags after adding resource tags: %s", desiredTags));
        }

        // get resource level tags from resource model based on your tag property name
        if (handlerRequest.getDesiredResourceState().getTags() != null) {
            final List<Tag> tags = Translator.cfnTagsToSdkTags(handlerRequest.getDesiredResourceState().getTags());
            desiredTags.putAll(convertToMap(tags));
            logger.log(String.format("Desired tags after adding resource state tags: %s", desiredTags));
        }

        return desiredTags;
    }

    /**
     * generateTagsToAdd
     *
     * Determines the tags the customer desired to define or redefine.
     */
    public static Map<String, String> generateTagsToAdd(final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        return desiredTags.entrySet().stream()
                .filter(e -> !previousTags.containsKey(e.getKey()) || !Objects.equals(previousTags.get(e.getKey()), e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    /**
     * getTagsToRemove
     *
     * Determines the tags the customer desired to remove from the function.
     */
    public static Set<String> generateTagsToRemove(final Map<String, String> previousTags, final Map<String, String> desiredTags) {
        final Set<String> desiredTagNames = desiredTags.keySet();

        return previousTags.keySet().stream()
                .filter(tagName -> !desiredTagNames.contains(tagName))
                .collect(Collectors.toSet());
    }

    /**
     * generateTagsToAdd
     *
     * Determines the tags the customer desired to define or redefine.
     */
    public static Set<Tag> generateTagsToAdd(final Set<Tag> previousTags, final Set<Tag> desiredTags) {
        return Sets.difference(new HashSet<>(desiredTags), new HashSet<>(previousTags));
    }

    /**
     * getTagsToRemove
     *
     * Determines the tags the customer desired to remove from the function.
     */
    public static Set<Tag> generateTagsToRemove(final Set<Tag> previousTags, final Set<Tag> desiredTags) {
        return Sets.difference(new HashSet<>(previousTags), new HashSet<>(desiredTags));
    }

    public static ProgressEvent<ResourceModel, CallbackContext> updateTags(
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger,
            final Action action) {

        // Chain progress
        ProgressEvent<ResourceModel, CallbackContext> progressToReturn;
        final Set<Tag> previousTags = convertToSet(getPreviouslyAttachedTags(request, logger));
        final Set<Tag> desiredTags = convertToSet(getNewDesiredTags(request, logger));

        // Remove any tags no longer in the template
        final Collection<Tag> resourceTagsToRemove = generateTagsToRemove(previousTags, desiredTags);
        progressToReturn = untagResource(progress, proxy, proxyClient, request, callbackContext, resourceTagsToRemove, logger);
        // Add new tags
        final Collection<Tag> resourceTagsToAdd = generateTagsToAdd(previousTags, desiredTags);
        return tagResource(progressToReturn, proxy, proxyClient, request, callbackContext, resourceTagsToAdd, logger, action);
    }

    /**
     * tagResource during update
     *
     * Calls the service:TagResource API.
     */
    private static ProgressEvent<ResourceModel, CallbackContext> tagResource(
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<SageMakerClient> serviceClient,
            final ResourceHandlerRequest<ResourceModel> handlerRequest,
            final CallbackContext callbackContext,
            final Collection<Tag> resourceTagsToAdd,
            final Logger logger,
            final Action action
    ) {
        if (!resourceTagsToAdd.isEmpty()) {
            logger.log(String.format("[UPDATE][IN PROGRESS] Adding tags for TrackingServer ARN %s",
                    handlerRequest.getDesiredResourceState().getTrackingServerArn()));
            return proxy.initiate("AWS-SageMaker-MlflowTrackingServer::TagResource", serviceClient, handlerRequest.getDesiredResourceState(), callbackContext)
                    .translateToServiceRequest(model -> Translator.translateToAddTagsRequest(resourceTagsToAdd, model.getTrackingServerArn()))
                    .makeServiceCall((request, client) -> proxy.injectCredentialsAndInvokeV2(request, client.client()::addTags))
                    .handleError((createRequest, exception, client, resourceModel, errorCallbackContext) -> {
                        logger.log(exception.getMessage());
                        final BaseHandlerException cfnException = ExceptionMapper.getCfnException(action.toString(), ResourceModel.TYPE_NAME,
                                resourceModel.getTrackingServerName(), (AwsServiceException) exception);
                        return ProgressEvent.defaultFailureHandler(cfnException, cfnException.getErrorCode());
                    })
                    .progress();
        }
        return progress;
    }

    /**
     * untagResource during update
     *
     * Calls the service:UntagResource API.
     */
    private static ProgressEvent<ResourceModel, CallbackContext> untagResource(
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<SageMakerClient> serviceClient,
            final ResourceHandlerRequest<ResourceModel> handlerRequest,
            final CallbackContext callbackContext,
            final Collection<Tag> resourceTagsToRemove,
            final Logger logger
    ) {
        if (!resourceTagsToRemove.isEmpty()) {
            logger.log(String.format("[UPDATE][IN PROGRESS] Removing tags for TrackingServer ARN %s",
                    handlerRequest.getDesiredResourceState().getTrackingServerArn()));
            return proxy.initiate("AWS-SageMaker-MlflowTrackingServer::UntagResource", serviceClient, handlerRequest.getDesiredResourceState(), callbackContext)
                    .translateToServiceRequest(model -> Translator.translateToDeleteTagsRequest(resourceTagsToRemove, model.getTrackingServerArn()))
                    .makeServiceCall((request, client) -> proxy.injectCredentialsAndInvokeV2(request, client.client()::deleteTags))
                    .progress();
        }
        return progress;
    }

}