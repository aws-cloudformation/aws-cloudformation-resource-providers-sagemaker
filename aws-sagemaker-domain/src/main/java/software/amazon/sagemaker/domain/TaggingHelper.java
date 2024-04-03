package software.amazon.sagemaker.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.AddTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class TaggingHelper {

    public static List<Tag> consolidateTags(ResourceHandlerRequest<ResourceModel> request) {
        Map<String, String> systemTags = request.getSystemTags();
        List<Tag> customerTags = request.getDesiredResourceState().getTags();
        Map<String, String> resourceTags = request.getDesiredResourceTags();

        Map<String, String> tags = new HashMap<>();

        if (resourceTags != null) {
            tags.putAll(resourceTags);
        }

        if (systemTags != null) {
            tags.putAll(systemTags);
        }

        if (customerTags != null) {
            for (Tag e : customerTags) {
                tags.put(e.getKey(), e.getValue());
            }
        }

        return tags.entrySet().stream().map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build()).collect(Collectors.toList());
    }

    @SuppressWarnings("resource")
    public static List<Tag> listTagsForResource(String resourceArn,
    ProxyClient<SageMakerClient> proxyClient) {
        ListTagsRequest listTagsRequest = ListTagsRequest.builder().resourceArn(resourceArn).build();
        ListTagsResponse listTagsResponse = proxyClient.injectCredentialsAndInvokeV2(listTagsRequest, proxyClient.client()::listTags);
        return transformTags(listTagsResponse.tags()).stream().collect(Collectors.toList());
    }

    @SuppressWarnings("resource")
    public static void addTags(String resourceArn,
                               Set<Tag> tags,
                               ProxyClient<SageMakerClient> proxyClient) {
        AddTagsRequest addTagsRequest = AddTagsRequest.builder()
            .resourceArn(resourceArn)
            .tags(tags.stream().map(tag -> software.amazon.awssdk.services.sagemaker.model.Tag.builder().key(tag.getKey()).value(tag.getValue()).build()).collect(Collectors.toList()))
            .build();
        proxyClient.injectCredentialsAndInvokeV2(addTagsRequest, proxyClient.client()::addTags);
    }

    @SuppressWarnings("resource")
    public static void removeTags(String resourceArn,
                                  Set<Tag> tags,
                                  ProxyClient<SageMakerClient> proxyClient) {
        Collection<String> tagKeys = tags.stream().map(Tag::getKey).collect(Collectors.toSet());
        DeleteTagsRequest deleteTagsRequest = DeleteTagsRequest.builder().resourceArn(resourceArn).tagKeys(tagKeys).build();
        proxyClient.injectCredentialsAndInvokeV2(deleteTagsRequest, proxyClient.client()::deleteTags);
    }

    public static void updateTags(String resourceArn,
                                  Set<Tag> previousTags,
                                  Set<Tag> currentTags,
                                  ProxyClient<SageMakerClient> proxyClient) {
        Set<Tag> tagsToAdd = Sets.difference(currentTags, previousTags);
        Set<Tag> tagsToRemove = Sets.difference(previousTags, currentTags);
        if (!tagsToRemove.isEmpty()) {
            removeTags(resourceArn, tagsToRemove, proxyClient);
        }
        if (!tagsToAdd.isEmpty()) {
            addTags(resourceArn, tagsToAdd, proxyClient);
        }
    }

    public static Set<Tag> transformTags(List<software.amazon.awssdk.services.sagemaker.model.Tag> tags) {
        Set<Tag> filteredTags = new HashSet<>();
        if (tags != null) {
            for (software.amazon.awssdk.services.sagemaker.model.Tag e : tags) {
                if (!e.key().toLowerCase().startsWith("aws:")) {
                    filteredTags.add(Tag.builder().key(e.key()).value(e.value()).build());
                }
            }
        }

        return filteredTags;
    }

    public static Set<Tag> transformTags(Map<String, String> tags) {
        Set<Tag> filteredTags = new HashSet<>();
        if (tags != null) {
            for (Map.Entry<String, String> e : tags.entrySet()) {
                if (!e.getKey().toLowerCase().startsWith("aws:")) {
                    filteredTags.add(Tag.builder().key(e.getKey()).value(e.getValue()).build());
                }
            }
        }

        return filteredTags;
    }

}
