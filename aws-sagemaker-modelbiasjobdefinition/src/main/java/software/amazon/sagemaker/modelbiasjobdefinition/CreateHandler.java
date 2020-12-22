package software.amazon.sagemaker.modelbiasjobdefinition;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateModelBiasJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Random;


public class CreateHandler extends BaseHandlerStd {
    public static final int ALLOWED_JOB_DEFINITION_NAME_LENGTH = 20;
    public static final String CFN_RESOURCE_NAME_PREFIX = "CFN";
    public static final int GUID_LENGTH = 12;

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        //Set job definition name if absent
        String jobDefinitionName = model.getJobDefinitionName();
        if(StringUtils.isEmpty(jobDefinitionName)){
            jobDefinitionName = generateParameterName(request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken());
            model.setJobDefinitionName(jobDefinitionName);
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                    proxy.initiate("AWS-SageMaker-ModelBiasJobDefinition::Create", proxyClient, model, callbackContext)
                        .translateToServiceRequest(TranslatorForRequest::translateToCreateRequest)
                        .makeServiceCall(this::createResource)
                        .progress())
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param awsRequest the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create resource response
     */
    private CreateModelBiasJobDefinitionResponse createResource(
            final CreateModelBiasJobDefinitionRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        CreateModelBiasJobDefinitionResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createModelBiasJobDefinition);
        } catch (final ResourceInUseException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, awsRequest.jobDefinitionName());
        } catch (final AwsServiceException e) {

            // The exception thrown due to validation failure does not have error code set,
            // hence we need to check it using error message
            if(StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("validation error detected")) {
                throw new CfnInvalidRequestException(Action.CREATE.toString(), e);
            }
            Translator.throwCfnException(Action.CREATE.toString(), e);
        }

        return response;
    }

    // We support this special use case of auto-generating names only for CloudFormation.
    // Name format: Prefix - logical resource id - randomString
    private String generateParameterName(final String logicalResourceId, final String clientRequestToken) {
        StringBuilder sb = new StringBuilder();
        int endIndex = logicalResourceId.length() > ALLOWED_JOB_DEFINITION_NAME_LENGTH
                ? ALLOWED_JOB_DEFINITION_NAME_LENGTH : logicalResourceId.length();

        sb.append(CFN_RESOURCE_NAME_PREFIX);
        sb.append("-");
        sb.append(logicalResourceId.substring(0, endIndex));
        sb.append("-");

        sb.append(RandomStringUtils.random(
                GUID_LENGTH,
                0,
                0,
                true,
                true,
                null,
                new Random(clientRequestToken.hashCode())));
        return sb.toString();
    }

}