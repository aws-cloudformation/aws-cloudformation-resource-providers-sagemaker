package software.amazon.sagemaker.modelbiasjobdefinition;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelBiasJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteModelBiasJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

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
            jobDefinitionName = Utils.getResourceNameFromArn(model.getJobDefinitionArn(), MODEL_BIAS_ARN_SUBSTRING);
            model.setJobDefinitionName(jobDefinitionName);
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SageMaker-ModelBiasJobDefinition::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     *
     * @param awsRequest  the aws service request to delete a resource
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private DeleteModelBiasJobDefinitionResponse deleteResource(
            final DeleteModelBiasJobDefinitionRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteModelBiasJobDefinitionResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteModelBiasJobDefinition);
        } catch (ResourceNotFoundException e) {
            // NotFound responded from Delete handler will be considered as success by CFN backend service.
            // This is to handle out of stack resource deletion (https://sage.amazon.com/questions/896677)
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.jobDefinitionName());
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.DELETE.toString(), e);
        }

        return response;
    }
}
