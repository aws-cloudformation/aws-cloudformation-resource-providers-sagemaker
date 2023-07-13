package software.amazon.sagemaker.pipeline;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TranslatorTest extends AbstractTestBase {

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ResourceInUse() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, resourceInUseException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ResourceNotFound() {
        final ResourceNotFoundException resourceNotFoundException = ResourceNotFoundException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(404)
                .build();

        Exception exception = assertThrows(CfnNotFoundException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, resourceNotFoundException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotFound.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ResourceLimitExceeded() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, resourceLimitExceededException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, TEST_ERROR_MESSAGE));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_UnauthorizedOperation() {
        final AwsServiceException unauthorizedOperationException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .errorCode("UnauthorizedOperation")
                        .build())
                .statusCode(403)
                .build();

        Exception exception = assertThrows(CfnAccessDeniedException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, unauthorizedOperationException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AccessDenied.getMessage(),
                TEST_ERROR_MESSAGE));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ValidationException() {
        final AwsServiceException validationFailureExceptionNullName = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ValidationException")
                        .errorMessage("Value null at 'pipelineName' failed to " +
                                "satisfy constraint: Member must not be null")
                        .build())
                .statusCode(400)
                .build();

        Exception exception = assertThrows(CfnInvalidRequestException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, validationFailureExceptionNullName));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                validationFailureExceptionNullName.awsErrorDetails().errorMessage()));

        final AwsServiceException validationFailureExceptionUniqueName = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ValidationException")
                        .errorMessage("names must be unique within an AWS account and region")
                        .build())
                .statusCode(400)
                .build();

        Exception exception2 = assertThrows(CfnAlreadyExistsException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, validationFailureExceptionUniqueName));

        assertThat(exception2.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_InternalError() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .errorCode("InternalError")
                        .build())
                .statusCode(500)
                .build();

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, serviceInternalException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                TEST_ERROR_MESSAGE));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ServiceUnavailable() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .errorCode("ServiceUnavailable")
                        .build())
                .statusCode(500)
                .build();

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, serviceInternalException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                TEST_ERROR_MESSAGE));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ThrottlingException() {
        final AwsServiceException throttlingException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .errorCode("ThrottlingException")
                        .build())
                .statusCode(400)
                .build();

        Exception exception = assertThrows(CfnThrottlingException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, throttlingException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.Throttling.getMessage(),
                TEST_ERROR_MESSAGE));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_UnknownException() {
        final AwsServiceException unknownException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .errorCode("Unknown")
                        .build())
                .statusCode(400)
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, unknownException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                TEST_ERROR_MESSAGE));
    }

    @Test
    public void testGetHandlerError_NoErrorCode() {
        final AwsServiceException noErrorCodeException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder().build())
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, noErrorCodeException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                TEST_OPERATION));
    }

    @Test
    public void testGetHandlerError_NoErrorDetails() {
        AwsServiceException noDetailsException = SageMakerException.builder().build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ResourceModel.TYPE_NAME, TEST_PIPELINE_NAME, noDetailsException));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                TEST_OPERATION));
    }
}