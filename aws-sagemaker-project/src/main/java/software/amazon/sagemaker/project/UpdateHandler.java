package software.amazon.sagemaker.project;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<SageMakerClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> addProjectArnIfNotAvailable(proxyClient, model, callbackContext))
                .then(progress -> updateTags(proxyClient, model, callbackContext))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Adding the project arn, if not available in the model
     * and validate input fields are not modified
     *
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return progressEvent, in progress with delay callback and model state
     */
    private ProgressEvent<ResourceModel, CallbackContext> addProjectArnIfNotAvailable(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            if (model.getProjectArn() == null) {
                DescribeProjectResponse response = proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToReadRequest(model), proxyClient.client()::describeProject);

                model.setProjectArn(response.projectArn());

                // validate no changes to input fields
                if (false == Translator.compareRequiredFields(model, response)) {
                    throw new CfnInvalidRequestException("Update not supported");
                }
            }
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getProjectName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getProjectName(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }


    /**
     * Client invocation of the update tags request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return progressEvent, in progress with delay callback and model state
     */
    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            handleTagging(proxyClient, model);
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getProjectName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getProjectName(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Validate no tag difference between existing model and updated model
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     */
    private void handleTagging(final ProxyClient<SageMakerClient> proxyClient,
                               final ResourceModel model) {
        final Set<software.amazon.awssdk.services.sagemaker.model.Tag> newTags = new HashSet<>(Translator.cfnTagsToSdkTags(model.getTags()));
        final Set<software.amazon.awssdk.services.sagemaker.model.Tag> existingTags
                = new HashSet<>(proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToListTagsRequest(model), proxyClient.client()::listTags).tags());

        final List<String> tagsToRemove = existingTags.stream()
                .filter(tag -> !newTags.contains(tag))
                .map(tag -> tag.key())
                .collect(Collectors.toList());
        final List<Tag> tagsToAdd = newTags.stream()
                .filter(tag -> !existingTags.contains(tag))
                .collect(Collectors.toList());
        if (false == (tagsToRemove.isEmpty() && tagsToAdd.isEmpty())) {
            throw new CfnInvalidRequestException("Tag update not supported");
        }
    }
}
