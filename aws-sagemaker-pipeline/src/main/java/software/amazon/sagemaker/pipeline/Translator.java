package software.amazon.sagemaker.pipeline;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class contains translation methods for object other than api request/response.
 * It also contains common methods required by other translators.
 */
public class Translator {

    /**
     * Throws Cfn exception corresponding to error code of the given exception.
     *
     * @param operation operation
     * @param resourceType resource type
     * @param resourceName resource name
     * @param e exception
     */
    static void throwCfnException(
            final String operation,
            final String resourceType,
            final String resourceName,
            final AwsServiceException e
    ) {

        if (e instanceof ResourceInUseException) {
            throw new ResourceAlreadyExistsException(resourceType, resourceName, e);
        }

        if (e instanceof ResourceNotFoundException) {
            throw new CfnNotFoundException(resourceType, resourceName, e);
        }

        if (e instanceof ResourceLimitExceededException) {
            throw new CfnServiceLimitExceededException(resourceType, e.getMessage(), e);
        }

        if(e.awsErrorDetails() != null && StringUtils.isNotBlank(e.awsErrorDetails().errorCode())) {
            String errorMessage = e.awsErrorDetails().errorMessage();
            switch (e.awsErrorDetails().errorCode()) {
                case "UnauthorizedOperation":
                    throw new CfnAccessDeniedException(errorMessage, e);
                case "ValidationException":
                    throw new CfnInvalidRequestException(errorMessage, e);
                case "InternalError":
                case "ServiceUnavailable":
                    throw new CfnServiceInternalErrorException(errorMessage, e);
                case "ThrottlingException":
                    throw new CfnThrottlingException(errorMessage, e);
                default:
                    throw new CfnGeneralServiceException(errorMessage, e);
            }
        }

        throw new CfnGeneralServiceException(operation, e);
    }

    static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}