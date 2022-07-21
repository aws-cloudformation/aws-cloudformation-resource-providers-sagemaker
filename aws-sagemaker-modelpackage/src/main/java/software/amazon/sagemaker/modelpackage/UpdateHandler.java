package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.Tag;
import software.amazon.awssdk.services.sagemaker.model.AdditionalInferenceSpecificationDefinition;
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
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.amazonaws.util.CollectionUtils;
import java.util.Optional;

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
            .then(progress -> addModelPackageArnIfNotAvailable(proxyClient, model, callbackContext))
            .then(progress -> updateModelPackageApiCall(proxyClient, model, callbackContext))
            .then(progress -> updateTags(proxyClient, model, callbackContext))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> addModelPackageArnIfNotAvailable(
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext) {
        try {
            if (model.getModelPackageArn() == null && model.getModelPackageName() != null) {
                DescribeModelPackageResponse response = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToReadRequest(model), proxyClient.client()::describeModelPackage);
                model.setModelPackageArn(response.modelPackageArn());
            }
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getModelPackageName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getModelPackageName(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    private void xorValidation(String modelPackageGroupName, String modelPackageName, String modelPackageArn) {
        final String updateNotPossibleMessage = String.format("Either ModelPackageName or ModelPackageGroupName should be present to update ModelPackage for %s.",modelPackageArn);
        if ((modelPackageGroupName == null && modelPackageName == null) || (modelPackageGroupName != null && modelPackageName != null)) {
            ExceptionMapper.throwCfnException(updateNotPossibleMessage);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelPackageApiCall(
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext) {
        try {
            xorValidation(model.getModelPackageGroupName(), model.getModelPackageName(), model.getModelPackageArn());
            if (model.getModelPackageGroupName() != null) {
                handleUpdateModelPackageRequest(proxyClient, model);
            }
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getModelPackageArn(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getModelPackageArn(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    private void handleUpdateModelPackageRequest(final ProxyClient<SageMakerClient> proxyClient,
                               final ResourceModel model) {
        final Map<String, String> newProperties = Translator.translateMapOfObjectsToMapOfStrings(model.getCustomerMetadataProperties());
        DescribeModelPackageResponse response = proxyClient.injectCredentialsAndInvokeV2(
            Translator.translateToReadRequest(model), proxyClient.client()::describeModelPackage);
        List<String> propertiesToRemove = null;
        if (response.customerMetadataProperties() != null) {
            final Map<String, String> existingProperties
                = new java.util.HashMap<String, String>(response.customerMetadataProperties());
            propertiesToRemove = existingProperties.keySet().stream()
                .filter(key -> !newProperties.containsKey(key))
                .collect(Collectors.toList());
        }

        List<AdditionalInferenceSpecificationDefinition> additionalInferenceToAdd = new java.util.ArrayList<>(
            Optional.ofNullable(Translator.cfnAdditionalInferenceSpecificationToSdk(model.getAdditionalInferenceSpecificationsToAdd())).orElse(new java.util.ArrayList<>()));

        proxyClient.injectCredentialsAndInvokeV2(
            Translator.translateToUpdateRequest(model, propertiesToRemove, additionalInferenceToAdd), proxyClient.client()::updateModelPackage);

    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext) {
        try {
            if (model.getModelPackageName() != null) {
                handleTagging(proxyClient, model);
            }
        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getModelPackageArn(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getModelPackageArn(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Find the tag difference between existing model and updated model and update the diff tags to the model package
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     */
    private void handleTagging(final ProxyClient<SageMakerClient> proxyClient,
                               final ResourceModel model) {
        final Set<Tag> newTags = new HashSet<Tag>(Translator.cfnTagsToSdkTags(model.getTags()));
        final Set<Tag> existingTags
            = new java.util.HashSet<Tag>(proxyClient.injectCredentialsAndInvokeV2(
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
                Translator.translateToDeleteTagsRequest(tagsToRemove, model.getModelPackageArn()),
                proxyClient.client()::deleteTags);
        }
        if (!CollectionUtils.isNullOrEmpty(tagsToAdd)) {
            proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToAddTagsRequest(tagsToAdd, model.getModelPackageArn()),
                proxyClient.client()::addTags);
        }
    }
}
