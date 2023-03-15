package software.amazon.sagemaker.inferenceexperiment;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TranslatorTest {

    public static final String TEST_OPERATION = "someOperation";

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_UnauthorizedOperation() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("UnauthorizedOperation").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        Exception exception = assertThrows(CfnAccessDeniedException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ex));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AccessDenied.getMessage(),
                TEST_OPERATION));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_InvalidParameter() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("InvalidParameter").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        assertThrows(CfnInvalidRequestException.class, () -> Translator.throwCfnException(TEST_OPERATION, ex));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_InvalidParameterValue() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("InvalidParameterValue").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        assertThrows(CfnInvalidRequestException.class, () -> Translator.throwCfnException(TEST_OPERATION, ex));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ValidationError() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("ValidationError").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        assertThrows(CfnInvalidRequestException.class, () -> Translator.throwCfnException(TEST_OPERATION, ex));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_InternalError() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("InternalError").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ex));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                TEST_OPERATION));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ServiceUnavailable() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("ServiceUnavailable").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ex));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                TEST_OPERATION));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ResourceLimitExceeded() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("ResourceLimitExceeded").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        assertThrows(CfnServiceLimitExceededException.class, () -> Translator.throwCfnException(TEST_OPERATION, ex));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ResourceNotFound() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("ResourceNotFound").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        assertThrows(CfnNotFoundException.class, () -> Translator.throwCfnException(TEST_OPERATION, ex));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_ThrottlingException() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("ThrottlingException").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        Exception exception = assertThrows(CfnThrottlingException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ex));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.Throttling.getMessage(),
                TEST_OPERATION));
    }

    @Test
    public void testGetHandlerErrorCodeForSageMakerErrorCode_UnknownException() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().errorCode("Unknown").build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ex));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                TEST_OPERATION));
    }

    @Test
    public void testGetHandlerError_NoErrorCode() {
        AwsErrorDetails errorDetails = AwsErrorDetails.builder().build();
        AwsServiceException ex = SageMakerException.builder().awsErrorDetails(errorDetails).build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ex));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                TEST_OPERATION));
    }

    @Test
    public void testGetHandlerError_NoErrorDetails() {
        AwsServiceException ex = SageMakerException.builder().build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () ->
                Translator.throwCfnException(TEST_OPERATION, ex));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                TEST_OPERATION));
    }
}