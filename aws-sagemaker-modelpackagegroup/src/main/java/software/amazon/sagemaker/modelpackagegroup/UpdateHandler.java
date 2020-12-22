package software.amazon.sagemaker.modelpackagegroup;

import com.amazonaws.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
                .then(progress -> addModelPackageGroupArnIfNotAvailable(proxyClient, model, callbackContext))
                .then(progress -> updateResourcePolicy(proxyClient, model, callbackContext))
                .then(progress -> updateTags(proxyClient, model, callbackContext))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Adding the model package group arn if not available in the model
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return progressEvent, in progress with delay callback and model state
     */
    private ProgressEvent<ResourceModel, CallbackContext> addModelPackageGroupArnIfNotAvailable(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            if (model.getModelPackageGroupArn() == null) {
                DescribeModelPackageGroupResponse response = proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToReadRequest(model), proxyClient.client()::describeModelPackageGroup);
                model.setModelPackageGroupArn(response.modelPackageGroupArn());
            }
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getModelPackageGroupName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getModelPackageGroupName(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Client invocation of the update resource policy request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return progressEvent, in progress with delay callback and model state
     */
    private ProgressEvent<ResourceModel, CallbackContext> updateResourcePolicy(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            if (model.getModelPackageGroupPolicy() != null) {
                proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToPutModelPackageGroupPolicyRequest(model), proxyClient.client()::putModelPackageGroupPolicy);
            } else {
                try {
                    proxyClient.injectCredentialsAndInvokeV2(
                            Translator.translateToDeleteModelPackageGroupPolicyRequest(model), proxyClient.client()::deleteModelPackageGroupPolicy);
                } catch (AwsServiceException e) {
                    if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().matches(".*Cannot find resource policy.*")) {
                        // policy already deleted or not available
                    }
                    else {
                        throw e;
                    }
                }
            }
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getModelPackageGroupName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getModelPackageGroupName(), e);
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
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getModelPackageGroupName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getModelPackageGroupName(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Find the tag difference between existing model and updated model and update the diff tags to the model package group
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     */
    private void handleTagging(final ProxyClient<SageMakerClient> proxyClient,
                                           final ResourceModel model) {
        final Set<Tag> newTags = new HashSet<>(Translator.cfnTagsToSdkTags(model.getTags()));
        final Set<Tag> existingTags
                = new HashSet<>(proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToListTagsRequest(model), proxyClient.client()::listTags).tags());
        final List<String> tagsToRemove = existingTags.stream()
                .filter(tag -> !newTags.contains(tag))
                .map(tag -> tag.key())
                .collect(Collectors.toList());
        final List<Tag> tagsToAdd = newTags.stream()
                .filter(tag -> !existingTags.contains(tag))
                .collect(Collectors.toList());

        if (!CollectionUtils.isNullOrEmpty(tagsToRemove)) {
            proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToDeleteTagsRequest(tagsToRemove, model.getModelPackageGroupArn()),
                    proxyClient.client()::deleteTags);
        }
        if (!CollectionUtils.isNullOrEmpty(tagsToAdd)) {
            proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToAddTagsRequest(tagsToAdd, model.getModelPackageGroupArn()),
                    proxyClient.client()::addTags);
        }
    }
}
