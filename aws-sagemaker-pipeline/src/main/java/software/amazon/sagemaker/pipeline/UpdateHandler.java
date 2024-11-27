package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdatePipelineResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

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
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToUpdateRequest)
                                .makeServiceCall(this::updateResource)
                                .progress())
                .then(progress -> updateTags(proxyClient, model, callbackContext))
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

    /**
     * Handles updating tags on the Pipeline resource if changes are present.
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return progressEvent, in progress with delay callback and model state
     */
    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            handleTagging(proxyClient, model);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME,
                    model.getPipelineName(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Identify the tag difference between existing and desired resource state. Add or delete tags on the pipeline.
     * resource as necessary.
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     */
    private void handleTagging(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {
        final String pipelineArn = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model), proxyClient.client()::describePipeline).pipelineArn();

        final Set<software.amazon.awssdk.services.sagemaker.model.Tag> newTags =
                new HashSet<>(Translator.cfnTagsToSdkTags(model.getTags()));
        final Set<software.amazon.awssdk.services.sagemaker.model.Tag> existingTags =
                new HashSet<>(proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToListTagsRequest(pipelineArn), proxyClient.client()::listTags).tags());


        final List<software.amazon.awssdk.services.sagemaker.model.Tag> tagsToAdd = newTags.stream()
                .filter(tag -> !existingTags.contains(tag))
                .collect(Collectors.toList());
        final List<String> tagsToAddKeys = tagsToAdd.stream()
                .map(software.amazon.awssdk.services.sagemaker.model.Tag::key)
                .collect(Collectors.toList());
        final List<String> tagsToRemove = existingTags.stream()
                .filter(tag -> !newTags.contains(tag) && !tagsToAddKeys.contains(tag.key()))
                .map(software.amazon.awssdk.services.sagemaker.model.Tag::key)
                .collect(Collectors.toList());
        if (!tagsToRemove.isEmpty()) {
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToDeleteTagsRequest(tagsToRemove, pipelineArn),
                    proxyClient.client()::deleteTags);
        }
        if (!tagsToAdd.isEmpty()) {
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToAddTagsRequest(tagsToAdd, pipelineArn),
                    proxyClient.client()::addTags);
        }
    }

}
