package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Pipeline::Update";
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SageMakerClient> proxyClient,
            Logger logger
    ) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        if (model.getPipelineDefinition().getPipelineDefinitionS3Location() != null) {
            String pipelineDefinition = S3ClientWrapper.getBodyFromS3(
                    model.getPipelineDefinition().getPipelineDefinitionS3Location(),
                    proxy,
                    logger
            );
            model.setPipelineDefinition(PipelineDefinition.builder()
                    .pipelineDefinitionBody(pipelineDefinition)
                    .build()
            );
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToUpdateRequest)
                                .makeServiceCall(this::updateResource)
                                .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the update request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param awsRequest the aws service request to update a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse update resource response
     */
    private UpdatePipelineResponse updateResource(
            final UpdatePipelineRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient
    ) {
        UpdatePipelineResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::updatePipeline);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.pipelineName(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, awsRequest.pipelineName(), e);
        }
        return response;
    }

}
