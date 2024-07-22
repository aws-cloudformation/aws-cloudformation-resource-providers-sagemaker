package software.amazon.sagemaker.mlflowtrackingserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest extends AbstractTestBase {

    @Test
    public void testTranslator() {
        assertThat(new Translator()).isInstanceOf(Translator.class);
    }

    @Test
    public void testCfnTagsToSdkTags_NullInputValue() {
        assertThat(Translator.cfnTagsToSdkTags(null)).isEmpty();
    }

    @Test
    public void testCfnTagsToSdkTags_NullTagKey() {
        List<software.amazon.sagemaker.mlflowtrackingserver.Tag> inputTags = new ArrayList<>();
        inputTags.add(software.amazon.sagemaker.mlflowtrackingserver.Tag.builder().key(null).value("value1").build());
        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> Translator.cfnTagsToSdkTags(inputTags));
        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Tags cannot have a null key"));
    }

    @Test
    public void testCfnTagsToSdkTags_NullTagValue() {
        List<software.amazon.sagemaker.mlflowtrackingserver.Tag> inputTags = new ArrayList<>();
        inputTags.add(software.amazon.sagemaker.mlflowtrackingserver.Tag.builder().key("key1").value(null).build());
        final Exception exception = assertThrows(CfnInvalidRequestException.class, () -> Translator.cfnTagsToSdkTags(inputTags));
        assertThat(exception.getMessage()).isEqualTo(String.format(
                HandlerErrorCode.InvalidRequest.getMessage(), "Tags cannot have a null value"));
    }
}
