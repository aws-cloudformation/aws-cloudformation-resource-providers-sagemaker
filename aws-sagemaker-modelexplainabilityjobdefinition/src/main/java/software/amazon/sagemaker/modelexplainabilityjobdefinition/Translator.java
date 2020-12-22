package software.amazon.sagemaker.modelexplainabilityjobdefinition;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;

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
     * @param operation
     * @param e exception
     */
    public static void throwCfnException(final String operation, final AwsServiceException e) {
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

    public static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

}
