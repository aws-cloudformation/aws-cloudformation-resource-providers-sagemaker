package software.amazon.sagemaker.mlflowtrackingserver;

/**
 * A helper class to maintain all model change checks.
 */
public final class ImmutabilityHelper {

    static boolean isRoleArnEquivalent(final ResourceModel previous, final ResourceModel desired) {
        // Only allow update requests if the RoleArn is the same between the previous and desired states.
        return previous.getRoleArn().equals(desired.getRoleArn());
    }

    static boolean isMlflowVersionEquivalent(final ResourceModel previous, final ResourceModel desired) {
        // Only allow update requests if the MLFlow Version is the same between the previous and desired states.
        return desired.getMlflowVersion() == null ||
                (previous.getMlflowVersion() != null && previous.getMlflowVersion().equals(desired.getMlflowVersion()));
    }

    public static boolean isChangeMutable(final ResourceModel previous, final ResourceModel desired) {
        return isMlflowVersionEquivalent(previous, desired) && isRoleArnEquivalent(previous, desired);
    }
}
