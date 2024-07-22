package software.amazon.sagemaker.mlflowtrackingserver;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;

/**
 * Mapper class that helps convert general service exceptions into the corresponding CFN exception.
 */
public class ExceptionMapper {

    /**
     * Returns a CFN exception corresponding to error code of the given exception.
     * @param operation CFN operation being invoked
     * @param e exception the service exception caught in handler
     * @return CFN exception mapped from the service exception
     */
    static BaseHandlerException getCfnException(
            final String operation,
            final String resourceType,
            final String resourceName,
            final AwsServiceException e) {

        // The exception thrown due to validation failure does not have error code set,
        // hence we need to check it using error message
        if(StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("validation error detected")) {
            throw new CfnInvalidRequestException(operation, e);
        }

        if(e.awsErrorDetails() != null && StringUtils.isNotBlank(e.awsErrorDetails().errorCode())) {
            switch (e.awsErrorDetails().errorCode()) {
                case "UnauthorizedOperation":
                case "AccessDeniedException":
                    return new CfnAccessDeniedException(operation, e);
                case "InvalidParameter":
                case "InvalidParameterValue":
                case "ValidationError":
                    return new CfnInvalidRequestException(
                            formatExceptionMessage(operation, resourceType, resourceName), e);
                case "InternalError":
                case "ServiceUnavailable":
                    return new CfnServiceInternalErrorException(operation, e);
                case "ResourceLimitExceeded":
                    return new CfnServiceLimitExceededException(resourceType, "ResourceLimitExceeded", e);
                case "ResourceNotFound":
                    return new CfnNotFoundException(resourceType, resourceName, e);
                case "ResourceInUseException":
                    if (operation.equals(Action.CREATE.toString())) {
                        return new ResourceAlreadyExistsException(resourceType, resourceName, e);
                    } else {
                        return new CfnResourceConflictException(resourceType, resourceName, "ResourceInUseException", e);
                    }
                case "ThrottlingException":
                    return new CfnThrottlingException(operation, e);
                default:
                    throw e;
            }
        }
        throw e;
    }

    private static String formatExceptionMessage(
            final String operation,
            final String resourceType,
            final String resourceName) {
        return String.format("Exception caught for resource type: [%s] with name: [%s] during operation: [%s]",
                resourceType, resourceName, operation);
    }
}
