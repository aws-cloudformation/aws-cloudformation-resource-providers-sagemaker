package software.amazon.sagemaker.mlflowtrackingserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TagHelperTest extends AbstractTestBase {

    @Test
    public void testTagHelper() {
        // To increase line coverage
        assertThat(new TagHelper()).isInstanceOf(TagHelper.class);
    }

    @Test
    public void testConvertToMap_Empty() {
        assertThat(TagHelper.convertToMap(new HashSet<>())).isEqualTo(Collections.emptyMap());
    }

    @Test
    public void testConvertToMap_Success() {
        final Set<Tag> nullValueTagIncluded = new HashSet<>(TEST_SDK_TAGS);
        nullValueTagIncluded.add(Tag.builder().key("key2").value(null).build());
        assertThat(TagHelper.convertToMap(nullValueTagIncluded)).isEqualTo(TEST_CFN_TAGS);
    }

    @Test
    public void testConvertToMap_CollisionCheck() {
        final Set<Tag> tagSet = new HashSet<>();
        tagSet.add(Tag.builder().key("key1").value("value1").build());
        tagSet.add(Tag.builder().key("key1").value("value2").build());
        final Map<String, String> expectedTagMap = new HashMap<>();
        expectedTagMap.put("key1", "value2");
        assertThat(TagHelper.convertToMap(tagSet)).isEqualTo(expectedTagMap);
    }

    @Test
    public void testConvertToSet_Empty() {
        assertThat(TagHelper.convertToSet(new HashMap<>())).isEqualTo(Collections.emptySet());
    }

    @Test
    public void testConvertToSet_NullValue() {
        final Map<String, String> nullValueMap = TagHelper.convertToMap(TEST_SDK_TAGS);
        nullValueMap.put("key2", null);
        assertThat(TagHelper.convertToSet(nullValueMap)).isEqualTo(new HashSet<>(TEST_SDK_TAGS));
    }

    @Test
    public void testConvertToSet_Success() {
        assertThat(TagHelper.convertToSet(TEST_CFN_TAGS)).isEqualTo(new HashSet<>(TEST_SDK_TAGS));
    }

    @Test
    public void testShouldUpdateTagsReturnsTrue() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(false, false, false))
                .previousResourceTags(TEST_CFN_TAGS)
                .desiredResourceState(createResourceModel(false, false, false))
                .desiredResourceTags(TEST_CFN_TAGS_2)
                .build();
        assertThat(TagHelper.shouldUpdateTags(request, logger)).isTrue();
    }

    @Test
    public void testShouldUpdateTagsReturnsFalse() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(false, false, false))
                .previousResourceTags(TEST_CFN_TAGS)
                .desiredResourceState(createResourceModel(false, false, false))
                .desiredResourceTags(TEST_CFN_TAGS)
                .build();
        assertThat(TagHelper.shouldUpdateTags(request, logger)).isFalse();
    }

    @Test
    public void testShouldUpdateTagsReturnsFalse_MultipleTags() {
        Map<String, String> previousTags = ImmutableMap.of("key1", "value1", "key2", "value2");
        Map<String, String> desiredTags = ImmutableMap.of("key2", "value2", "key1", "value1");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(createResourceModel(false, false, false))
                .previousResourceTags(previousTags)
                .desiredResourceState(createResourceModel(false, false, false))
                .desiredResourceTags(desiredTags)
                .build();
        assertThat(TagHelper.shouldUpdateTags(request, logger)).isFalse();
    }

    @Test
    public void testGetPreviouslyAttachedTags_Null() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceTags(null)
                .build();
        assertThat(TagHelper.getPreviouslyAttachedTags(request, logger)).isEqualTo(Collections.emptyMap());
    }

    @Test
    public void testGetPreviouslyAttachedTags_Success() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceTags(TEST_CFN_TAGS)
                .build();
        assertThat(TagHelper.getPreviouslyAttachedTags(request, logger)).isEqualTo(TEST_CFN_TAGS);
    }

    @Test
    public void testGetNewDesiredTags_Null() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(null)
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        assertThat(TagHelper.getNewDesiredTags(request, logger)).isEqualTo(Collections.emptyMap());
    }

    @Test
    public void testGetNewDesiredTags_Success() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(TEST_CFN_TAGS)
                .desiredResourceState(createResourceModel(false, false, false))
                .build();
        assertThat(TagHelper.getNewDesiredTags(request, logger)).isEqualTo(TEST_CFN_TAGS);
    }

    @Test
    public void testGenerateTagsToAdd_inputStringMap_noOverlap() {
        assertThat(TagHelper.generateTagsToAdd(new HashMap<>(), TEST_CFN_TAGS)).isEqualTo(TEST_CFN_TAGS);
    }

    @Test
    public void testGenerateTagsToAdd_inputStringMap_withOverlappingKeyValuePair() {
        final Map<String, String> desiredTags = new HashMap<>();
        desiredTags.putAll(TEST_CFN_TAGS);
        desiredTags.putAll(TEST_CFN_TAGS_2);
        assertThat(TagHelper.generateTagsToAdd(TEST_CFN_TAGS, desiredTags)).isEqualTo(TEST_CFN_TAGS_2);
    }

    @Test
    public void testGenerateTagsToAdd_inputStringMap_withOverlappingKey() {
        final Map<String, String> previousTags = new HashMap<>();
        for (String key : TEST_CFN_TAGS.keySet()) {
            previousTags.put(key, "wrongValue");
        }
        assertThat(TagHelper.generateTagsToAdd(previousTags, TEST_CFN_TAGS)).isEqualTo(TEST_CFN_TAGS);
    }

    @Test
    public void generateTagsToRemove_inputStringMap_noOverlap() {
        assertThat(TagHelper.generateTagsToRemove(TEST_CFN_TAGS_2, TEST_CFN_TAGS)).isEqualTo(TEST_CFN_TAGS_2.keySet());
    }

    @Test
    public void generateTagsToRemove_inputStringMap_withOverlap() {
        final Map<String, String> previousTags = new HashMap<>();
        previousTags.putAll(TEST_CFN_TAGS);
        previousTags.putAll(TEST_CFN_TAGS_2);
        assertThat(TagHelper.generateTagsToRemove(previousTags, TEST_CFN_TAGS)).isEqualTo(TEST_CFN_TAGS_2.keySet());
    }

    @Test
    public void testGenerateTagsToAdd_inputTagSet() {
        final Set<Tag> testTags = new HashSet<>(TEST_SDK_TAGS);
        assertThat(TagHelper.generateTagsToAdd(new HashSet<>(), testTags)).isEqualTo(testTags);
    }

    @Test
    public void generateTagsToRemove_inputTagSet() {
        final Set<Tag> testPreviousTags = new HashSet<>(TEST_SDK_TAGS);
        final Set<Tag> testDesiredTags = new HashSet<>(TEST_RESOURCE_TAGS);
        assertThat(TagHelper.generateTagsToRemove(testPreviousTags, testDesiredTags)).isEqualTo(testPreviousTags);
    }

}
