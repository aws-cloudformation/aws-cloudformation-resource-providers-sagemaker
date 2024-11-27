package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.CreatePipelineResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Pipeline::Create";
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        if (model.getPipelineDefinition().getPipelineDefinitionS3Location() != null) {
            String pipelineDefinition = S3ClientWrapper.getBodyFromS3(
                    model.getPipelineDefinition().getPipelineDefinitionS3Location(),
                    proxy,
                    logger
            );
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
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
    private CreatePipelineResponse createResource(
            final CreatePipelineRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        CreatePipelineResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createPipeline);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, awsRequest.pipelineName(), e);
        }
        return response;
    }
}
