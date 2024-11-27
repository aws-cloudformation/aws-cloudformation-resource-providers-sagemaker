package software.amazon.sagemaker.modelpackage;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
        if(StringUtils.isNotBlank(e.getMessage())) {
            if (e.getMessage().contains("validation error detected")) {
                throw new CfnInvalidRequestException(e);
            }
            else if (e.awsErrorDetails() != null && StringUtils.isNotBlank(e.awsErrorDetails().errorCode())) {
                switch (e.awsErrorDetails().errorCode()) {
                    case "UnauthorizedOperation":
                        throw new CfnAccessDeniedException(e);
                    case "InvalidParameter":
                    case "InvalidParameterValue":
                    case "ValidationError":
                        throw new CfnInvalidRequestException(e);
                    case "InternalError":
                    case "ServiceUnavailable":
                        throw new CfnServiceInternalErrorException(e);
                    case "ResourceLimitExceeded":
                        throw new CfnServiceLimitExceededException(e);
                    case "ResourceNotFound":
                        throw new CfnNotFoundException(e);
                    case "ThrottlingException":
                        throw new CfnThrottlingException(e);
                    default:
                        throw new CfnGeneralServiceException(e);
                }
            }
            throw new CfnGeneralServiceException(e);
        }
        else {
            throw new CfnGeneralServiceException(operation, e);
        }
    }

    static void throwCfnException(final String operation, final String resourceType, final String resourceName, final AwsServiceException e) {
        if (StringUtils.isNotBlank(e.getMessage())) {
            if (e.getMessage().matches(".*Cannot find Model Package.*")
                || e.getMessage().matches(".*ModelPackage .* does not exist.*")) {
                throw new CfnNotFoundException(resourceType, resourceName, e);
            }
            else if (e.getMessage().matches(".*Model Package already exists.*")) {
                throw new ResourceAlreadyExistsException(resourceType, resourceName, e);
            }
            else if (e.getMessage().matches(".*not authorized to perform.*")) {
                throw new CfnAccessDeniedException(e);
            }
        }
        throwCfnException(operation, e);
    }

    static void throwCfnException(final String validationSpecificationFailureMessage) {
       throw new CfnInvalidRequestException(validationSpecificationFailureMessage);
    }

}
