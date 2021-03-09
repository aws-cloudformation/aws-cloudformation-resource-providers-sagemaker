package software.amazon.sagemaker.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionMapperTest {

    private static final String TEST_OPERATION = "testOperation";
    private static final String TEST_RESOURCE_TYPE = "testResourceType";
    private static final String TEST_RESOURCE_NAME = "testResourceName";
    private static final String TEST_ERROR_MESSAGE = "testErrorMessage";

    private static Stream<Arguments> provideExceptions(){
        return Stream.of(
                Arguments.of("UnauthorizedOperation", CfnAccessDeniedException.class),
                Arguments.of("InvalidParameter", CfnInvalidRequestException.class),
                Arguments.of("InvalidParameterValue", CfnInvalidRequestException.class),
                Arguments.of("ValidationError", CfnInvalidRequestException.class),
                Arguments.of("InternalError", CfnServiceInternalErrorException.class),
                Arguments.of("ResourceLimitExceeded", CfnServiceLimitExceededException.class),
                Arguments.of("ResourceNotFound", CfnNotFoundException.class),
                Arguments.of("ResourceInUseException", ResourceAlreadyExistsException.class),
                Arguments.of("ThrottlingException", CfnThrottlingException.class),
                Arguments.of("UnknownException", CfnGeneralServiceException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void testExceptionMapper(
            final String errorDetail,
            final Class<? extends BaseHandlerException> expectedCfnException) {
        final AwsServiceException awsServiceException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorCode(errorDetail).build())
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        assertThrows(expectedCfnException, () -> {
            throw ExceptionMapper.getCfnException(TEST_OPERATION,
                    TEST_RESOURCE_TYPE, TEST_RESOURCE_NAME, awsServiceException);
        });
    }

    @Test
    public void testExceptionMapper_ValidationException() {
        final AwsServiceException awsServiceException = SageMakerException.builder()
                .message("validation error detected")
                .statusCode(500)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> {
            throw ExceptionMapper.getCfnException(TEST_OPERATION,
                    TEST_RESOURCE_TYPE, TEST_RESOURCE_NAME, awsServiceException);
        });
    }
}
