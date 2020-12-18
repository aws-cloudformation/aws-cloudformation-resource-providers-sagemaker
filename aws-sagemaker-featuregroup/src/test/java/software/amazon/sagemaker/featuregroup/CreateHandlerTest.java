package software.amazon.sagemaker.featuregroup;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.FeatureGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.FeatureType;
import software.amazon.awssdk.services.sagemaker.model.OfflineStoreStatusValue;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceLimitExceededException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    private final ResourceModel requestModel = ResourceModel.builder()
            .featureGroupName(TEST_FEATURE_GROUP_NAME)
            .build();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<SageMakerClient> proxyClient;

    @Mock
    SageMakerClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(SageMakerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void testCreateHandler_SimpleSuccess() {
        List<software.amazon.awssdk.services.sagemaker.model.FeatureDefinition> featureDefinitions =
                Arrays.asList(
                        software.amazon.awssdk.services.sagemaker.model.FeatureDefinition.builder()
                                .featureName("year").featureType(FeatureType.INTEGRAL.toString()).build(),
                        software.amazon.awssdk.services.sagemaker.model.FeatureDefinition.builder()
                                .featureName("name").featureType(FeatureType.STRING.toString()).build()
                );

        software.amazon.awssdk.services.sagemaker.model.OnlineStoreConfig onlineStoreConfig =
                software.amazon.awssdk.services.sagemaker.model.OnlineStoreConfig.builder()
                .enableOnlineStore(true)
                .securityConfig(
                        software.amazon.awssdk.services.sagemaker.model
                                .OnlineStoreSecurityConfig.builder()
                                .kmsKeyId("kms").build()
                )
                .build();

        software.amazon.awssdk.services.sagemaker.model.OfflineStoreConfig offlineStoreConfig =
                software.amazon.awssdk.services.sagemaker.model.OfflineStoreConfig.builder()
                .dataCatalogConfig(software.amazon.awssdk.services.sagemaker.model.DataCatalogConfig.builder()
                        .catalog("c").database("d").tableName("t").build()
                )
                .s3StorageConfig(software.amazon.awssdk.services.sagemaker.model.S3StorageConfig.builder()
                        .s3Uri("s3").kmsKeyId("kms").build()
                )
                .disableGlueTableCreation(false)
                .build();

        final DescribeFeatureGroupResponse describeFeatureGroupResponse =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureDefinitions(featureDefinitions)
                        .onlineStoreConfig(onlineStoreConfig)
                        .offlineStoreConfig(offlineStoreConfig)
                        .featureGroupStatus(FeatureGroupStatus.CREATED)
                        .failureReason("none")
                        .offlineStoreStatus(software.amazon.awssdk.services.sagemaker.model.OfflineStoreStatus.builder()
                                .blockedReason("no")
                                .status(OfflineStoreStatusValue.ACTIVE)
                                .build())
                        .build();

        final CreateFeatureGroupResponse createFeatureGroupResponse = CreateFeatureGroupResponse.builder()
                .featureGroupArn(TEST_FEATURE_GROUP_ARN)
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenReturn(describeFeatureGroupResponse);
        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenReturn(createFeatureGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                .description(TEST_DESCRIPTION)
                .roleArn(TEST_ROLE_ARN)
                .featureDefinitions(Arrays.asList(
                        FeatureDefinition.builder().featureName("year").featureType(FeatureType.INTEGRAL.toString()).build(),
                        FeatureDefinition.builder().featureName("name").featureType(FeatureType.STRING.toString()).build()
                ))
                .onlineStoreConfig(OnlineStoreConfig
                        .builder()
                        .enableOnlineStore(true)
                        .securityConfig(OnlineStoreSecurityConfig.builder().kmsKeyId("kms").build())
                        .build()
                )
                .offlineStoreConfig(OfflineStoreConfig.builder()
                        .dataCatalogConfig(DataCatalogConfig.builder().catalog("c").database("d").tableName("t").build())
                        .s3StorageConfig(S3StorageConfig.builder().s3Uri("s3").kmsKeyId("kms").build())
                        .disableGlueTableCreation(false).build())
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_ServiceInternalException() {
        final AwsServiceException serviceInternalException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("InternalError")
                        .errorMessage(TEST_ERROR_MESSAGE)
                        .build())
                .statusCode(500)
                .build();

        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenThrow(serviceInternalException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnServiceInternalErrorException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceInternalError.getMessage(),
                serviceInternalException.awsErrorDetails().errorMessage()));
    }

    @Test
    public void testCreateHandler_FeatureGroupAlreadyExists_Fails() {
        final ResourceInUseException resourceInUseException = ResourceInUseException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenThrow(resourceInUseException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.AlreadyExists.getMessage(),
                ResourceModel.TYPE_NAME, TEST_FEATURE_GROUP_NAME));
    }

    @Test
    public void testCreateHandler_ResourceLimitExceededException() {
        final ResourceLimitExceededException resourceLimitExceededException = ResourceLimitExceededException.builder()
                .message(TEST_ERROR_MESSAGE)
                .statusCode(400)
                .build();

        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenThrow(resourceLimitExceededException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnServiceLimitExceededException.class, () -> invokeHandleRequest(request));
        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.ServiceLimitExceeded.getMessage(),
                ResourceModel.TYPE_NAME, TEST_ERROR_MESSAGE));
    }

    @Test
    public void testCreateHandler_ValidationFailure() {
        final AwsServiceException validationFailureException = SageMakerException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("ValidationException")
                        .errorMessage("Value null at 'featureGroupName' failed to " +
                                "satisfy constraint: Member must not be null")
                        .build())
                .statusCode(400)
                .build();

        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenThrow(validationFailureException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnInvalidRequestException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.InvalidRequest.getMessage(),
                validationFailureException.awsErrorDetails().errorMessage()));
    }

    @Test
    public void testCreateHandler_NoExceptionMessage() {
        final AwsServiceException someException = SageMakerException.builder()
                .statusCode(400)
                .build();

        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenThrow(someException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnGeneralServiceException.class, () -> invokeHandleRequest(request));

        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.GeneralServiceException.getMessage(),
                Action.CREATE));
    }

    @Test
    public void testCreateHandler_VerifyStabilization_EventualConsistency() {
        final DescribeFeatureGroupResponse describeFeatureGroupResponse2 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.CREATING)
                        .build();

        final DescribeFeatureGroupResponse describeFeatureGroupResponse3 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.CREATED)
                        .build();

        final CreateFeatureGroupResponse createFeatureGroupResponse = CreateFeatureGroupResponse.builder()
                .featureGroupArn(TEST_FEATURE_GROUP_ARN)
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build())
                .thenReturn(describeFeatureGroupResponse2).thenReturn(describeFeatureGroupResponse3);
        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenReturn(createFeatureGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                .description(TEST_DESCRIPTION)
                .roleArn(TEST_ROLE_ARN)
                .featureDefinitions(Collections.emptyList())
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_VerifyStabilization_Success() {
        final DescribeFeatureGroupResponse describeFeatureGroupResponse1 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.CREATING)
                        .build();

        final DescribeFeatureGroupResponse describeFeatureGroupResponse2 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.CREATED)
                        .build();

        final CreateFeatureGroupResponse createFeatureGroupResponse = CreateFeatureGroupResponse.builder()
                .featureGroupArn(TEST_FEATURE_GROUP_ARN)
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenReturn(describeFeatureGroupResponse1).thenReturn(describeFeatureGroupResponse2);
        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenReturn(createFeatureGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = invokeHandleRequest(request);

        ResourceModel expectedModelFromResponse = ResourceModel.builder()
                .featureGroupName(TEST_FEATURE_GROUP_NAME)
                .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                .description(TEST_DESCRIPTION)
                .roleArn(TEST_ROLE_ARN)
                .featureDefinitions(Collections.emptyList())
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(expectedModelFromResponse);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateHandler_VerifyStabilization_Failed() {
        final DescribeFeatureGroupResponse describeFeatureGroupResponse1 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.CREATING)
                        .build();

        final DescribeFeatureGroupResponse describeFeatureGroupResponse2 =
                DescribeFeatureGroupResponse.builder()
                        .featureGroupName(TEST_FEATURE_GROUP_NAME)
                        .eventTimeFeatureName(TEST_EVENT_TIME_FEATURE_NAME)
                        .recordIdentifierFeatureName(TEST_RECORD_ID_FEATURE_NAME)
                        .description(TEST_DESCRIPTION)
                        .roleArn(TEST_ROLE_ARN)
                        .creationTime(TEST_TIME)
                        .featureGroupStatus(FeatureGroupStatus.CREATE_FAILED)
                        .build();

        final CreateFeatureGroupResponse createFeatureGroupResponse = CreateFeatureGroupResponse.builder()
                .featureGroupArn(TEST_FEATURE_GROUP_ARN)
                .build();

        when(proxyClient.client().describeFeatureGroup(any(DescribeFeatureGroupRequest.class)))
                .thenReturn(describeFeatureGroupResponse1).thenReturn(describeFeatureGroupResponse2);
        when(proxyClient.client().createFeatureGroup(any(CreateFeatureGroupRequest.class)))
                .thenReturn(createFeatureGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .build();

        Exception exception = assertThrows(CfnNotStabilizedException.class, () -> invokeHandleRequest(request));
        assertThat(exception.getMessage()).isEqualTo(String.format(HandlerErrorCode.NotStabilized.getMessage(),
                ResourceModel.TYPE_NAME, TEST_FEATURE_GROUP_NAME));
    }

    private ProgressEvent<ResourceModel, CallbackContext> invokeHandleRequest(ResourceHandlerRequest<ResourceModel> request) {
        final CreateHandler handler = new CreateHandler();
        return handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
    }
}
