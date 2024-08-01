package software.amazon.sagemaker.mlflowtrackingserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
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
                Arguments.of(TEST_OPERATION, "UnauthorizedOperation", CfnAccessDeniedException.class),
                Arguments.of(TEST_OPERATION, "AccessDeniedException", CfnAccessDeniedException.class),
                Arguments.of(TEST_OPERATION, "InvalidParameter", CfnInvalidRequestException.class),
                Arguments.of(TEST_OPERATION, "InvalidParameterValue", CfnInvalidRequestException.class),
                Arguments.of(TEST_OPERATION, "ValidationError", CfnInvalidRequestException.class),
                Arguments.of(TEST_OPERATION, "InternalError", CfnServiceInternalErrorException.class),
                Arguments.of(TEST_OPERATION, "ResourceLimitExceeded", CfnServiceLimitExceededException.class),
                Arguments.of(TEST_OPERATION, "ResourceNotFound", CfnNotFoundException.class),
                Arguments.of(TEST_OPERATION, "ResourceInUseException", CfnResourceConflictException.class),
                Arguments.of("CREATE", "ResourceInUseException", ResourceAlreadyExistsException.class),
                Arguments.of(TEST_OPERATION, "ThrottlingException", CfnThrottlingException.class),
                Arguments.of(TEST_OPERATION, "UnknownException", SageMakerException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void testExceptionMapper(
            final String operation,
            final String errorDetail,
            final Class<? extends BaseHandlerException> expectedCfnException) {
        final AwsServiceException awsServiceException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorCode(errorDetail).build())
                .message(TEST_ERROR_MESSAGE)
                .statusCode(500)
                .build();

        assertThrows(expectedCfnException, () -> {
            throw ExceptionMapper.getCfnException(operation,
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

    @Test
    public void testExceptionMapper_NoErrorDetails() {
        final AwsServiceException awsServiceException = SageMakerException.builder()
                .statusCode(500)
                .build();

        assertThrows(AwsServiceException.class, () -> {
            throw ExceptionMapper.getCfnException(TEST_OPERATION,
                    TEST_RESOURCE_TYPE, TEST_RESOURCE_NAME, awsServiceException);
        });
    }

    @Test
    public void testExceptionMapper_NoErrorCode() {
        final AwsServiceException awsServiceException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder().build())
                .statusCode(500)
                .build();

        assertThrows(AwsServiceException.class, () -> {
            throw ExceptionMapper.getCfnException(TEST_OPERATION,
                    TEST_RESOURCE_TYPE, TEST_RESOURCE_NAME, awsServiceException);
        });
    }
}
