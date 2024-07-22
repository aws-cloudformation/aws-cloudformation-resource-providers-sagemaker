package software.amazon.sagemaker.mlflowtrackingserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ImmutabilityHelperTest extends AbstractTestBase {

    @Test
    public void testImmutabilityHelper() {
        // To increase line coverage
        assertThat(new ImmutabilityHelper()).isInstanceOf(ImmutabilityHelper.class);
    }

    @Test
    public void testIsRoleArnEquivalent_True() {
        final ResourceModel model = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        assertThat(ImmutabilityHelper.isRoleArnEquivalent(model, model)).isTrue();
    }

    @Test
    public void isMlflowVersionEquivalent_True() {
        final ResourceModel model = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        assertThat(ImmutabilityHelper.isMlflowVersionEquivalent(model, model)).isTrue();
    }

    @Test
    public void testIsChangeMutable_False_mismatchedRoleArn() {
        final ResourceModel previousModel = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        final ResourceModel desiredModel = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_UPDATED_ROLE_ARN);
        assertThat(ImmutabilityHelper.isChangeMutable(previousModel, desiredModel)).isFalse();
    }

    @Test
    public void testIsChangeMutable_True_desiredMlflowVersionNull() {
        final ResourceModel previousModel = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        final ResourceModel desiredModel = createResourceModel(false, false,false, null, TEST_DEFAULT_ROLE_ARN);
        assertThat(ImmutabilityHelper.isChangeMutable(previousModel, desiredModel)).isTrue();
    }

    @Test
    public void testIsChangeMutable_False_mismatchedMlflowVersion() {
        final ResourceModel previousModel = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        final ResourceModel desiredModel = createResourceModel(false, false,false, TEST_MINOR_UPGRADED_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        assertThat(ImmutabilityHelper.isChangeMutable(previousModel, desiredModel)).isFalse();
    }

    @Test
    public void testIsChangeMutable_False_previousVersionNull() {
        final ResourceModel previousModel = createResourceModel(false, false,false, null, TEST_DEFAULT_ROLE_ARN);
        final ResourceModel desiredModel = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        assertThat(ImmutabilityHelper.isChangeMutable(previousModel, desiredModel)).isFalse();
    }

    @Test
    public void testIsChangeMutable_True_allOtherPropertiesDifferent() {
        final ResourceModel previousModel = createResourceModel(false, false,false, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        final ResourceModel desiredModel = createResourceModel(true, true,true, TEST_MLFLOW_VERSION, TEST_DEFAULT_ROLE_ARN);
        assertThat(ImmutabilityHelper.isChangeMutable(previousModel, desiredModel)).isTrue();
    }
}
