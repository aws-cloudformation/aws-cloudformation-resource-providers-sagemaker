package software.amazon.sagemaker.project;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;

public class ExceptionMapper {
    /**
     * Throws Cfn exception corresponding to error code of the given exception.
     *
     * @param operation
     * @param e exception
     */
    static void throwCfnException(final String operation, final AwsServiceException e) {
        // The exception thrown due to validation failure does not have error code set,
        // hence we need to check it using error message
        if(StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("validation error detected")) {
            throw new CfnInvalidRequestException(operation, e);
        }
        if(e.awsErrorDetails() != null && StringUtils.isNotBlank(e.awsErrorDetails().errorCode())) {
            switch (e.awsErrorDetails().errorCode()) {
                case "UnauthorizedOperation":
                    throw new CfnAccessDeniedException(operation, e);
                case "InvalidParameter":
                case "InvalidParameterValue":
                case "ValidationError":
                    throw new CfnInvalidRequestException(operation, e);
                case "InternalError":
                case "ServiceUnavailable":
                    throw new CfnServiceInternalErrorException(operation, e);
                case "ResourceLimitExceeded":
                    throw new CfnServiceLimitExceededException(e);
                case "ResourceNotFound":
                    throw new CfnNotFoundException(e);
                case "ThrottlingException":
                    throw new CfnThrottlingException(operation, e);
                default:
                    throw new CfnGeneralServiceException(operation, e);
            }
        }
        throw new CfnGeneralServiceException(operation, e);
    }

    static void throwCfnException(final String operation, final String resourceType, final String resourceName, final AwsServiceException e) {
        if (StringUtils.isNotBlank(e.getMessage())
                && (e.getMessage().matches(".*Cannot find Project:.*")
                || e.getMessage().matches(".*Project .* does not exist.*"))) {
            throw new CfnNotFoundException(resourceType, resourceName, e);
        }
        if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().matches(".*Project already exists.*")) {
            throw new ResourceAlreadyExistsException(resourceType, resourceName, e);
        }
        throwCfnException(operation, e);
    }
}
