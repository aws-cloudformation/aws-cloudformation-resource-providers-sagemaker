package software.amazon.sagemaker.inferenceexperiment;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {
    public Configuration() {
        super("aws-sagemaker-inferenceexperiment.json");
    }

    /**
     * Providers should implement this method if their resource has a 'Tags' property to define
     * resource-level tags
     */
    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() == null) {
            return null;
        }
        return resourceModel.getTags().stream()
                .peek(Configuration::checkNulls)
                .collect(Collectors.toMap(Tag::getKey, Tag::getValue));
    }

    private static void checkNulls(final Tag tag) {
        if (tag.getKey() == null) {
            throw new CfnInvalidRequestException("Tags cannot have a null key");
        }
        if (tag.getValue() == null) {
            throw new CfnInvalidRequestException("Tags cannot have a null value");
        }
    }
}