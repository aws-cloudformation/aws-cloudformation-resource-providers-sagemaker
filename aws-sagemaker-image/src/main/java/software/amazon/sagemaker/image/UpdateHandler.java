package software.amazon.sagemaker.image;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.UpdateImageRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateImageResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


/**
 * CloudFormation resource handler to be invoked when updating an existing AWS::SageMaker::Image resource.
 */
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
            .then(progress ->
                proxy.initiate("AWS-SageMaker-Image::UpdateImage", proxyClient, model, progress.getCallbackContext())
                    .translateToServiceRequest((resourceModel -> getExistingStateAndTranslate(resourceModel, proxyClient)))
                    .makeServiceCall(this::updateImage)
                    .stabilize(this::stabilizedOnUpdate)
                    .progress()
            )
            .then(progress -> updateTags(proxyClient, model, callbackContext))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Read the current state of the image resource and use in conjunction with desired state to format an update.
     * image request.
     * @param requestedState CFN resource model representing the desired state of the resource
     * @param proxyClient the aws client used to make service calls
     * @return updateImageRequest the update request to be invoked by service
     */
    private UpdateImageRequest getExistingStateAndTranslate(
            final ResourceModel requestedState,
            final ProxyClient<SageMakerClient> proxyClient) {
        final DescribeImageResponse existingState;
        try {
            existingState = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(requestedState),
                            proxyClient.client()::describeImage);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, requestedState.getImageName(), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME,
                    requestedState.getImageName(), e);
        }
        return Translator.translateToUpdateRequest(requestedState, Translator.translateFromReadResponse(existingState));
    }

    /**
     * Invokes the update request using the provided proxyClient.
     * @param updateImageRequest the aws service request to update an image
     * @param proxyClient the aws client used to make service calls
     * @return updateImageResponse aws service response from updating an image resource
     */
    private UpdateImageResponse updateImage(
            final UpdateImageRequest updateImageRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final UpdateImageResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(updateImageRequest, proxyClient.client()::updateImage);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, updateImageRequest.imageName(), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME,
                    updateImageRequest.imageName(), e);
        }
        return response;
    }

    /**
     * Stabilization method to ensure that a recently updated image resource has moved from UPDATING status to CREATED.
     * @param updateImageRequest the aws service request to update an image
     * @param updateImageResponse the aws service response from updating an image resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the image resource has stabilized or not
     */
    private boolean stabilizedOnUpdate(
            final UpdateImageRequest updateImageRequest,
            final UpdateImageResponse updateImageResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if (model.getImageArn() == null) {
            model.setImageArn(updateImageResponse.imageArn());
        }

        final ImageStatus imageStatus = proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToReadRequest(model), proxyClient.client()::describeImage).imageStatus();

        switch (imageStatus) {
            case UPDATE_FAILED:
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getImageArn());
            case CREATED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getImageArn(), imageStatus));
                return true;
            case UPDATING:
                logger.log(String.format("%s [%s] is stabilizing %s.", ResourceModel.TYPE_NAME,
                        model.getImageArn(), imageStatus));
                return false;
            default:
                throw new CfnGeneralServiceException(
                        String.format("Stabilizing of %s failed with an unexpected status %s",
                                model.getImageArn(), imageStatus));
        }
    }

    /**
     * Handles updating tags on the Image resource if changes are present.
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
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getImageArn(), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME,
                    model.getImageName(), e);
        }
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Identify the tag difference between existing and desired resource state. Add or delete tags on the image.
     * resource as necessary.
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     */
    private void handleTagging(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {
        final Set<software.amazon.awssdk.services.sagemaker.model.Tag> newTags =
                new HashSet<>(Translator.cfnTagsToSdkTags(model.getTags()));
        final Set<software.amazon.awssdk.services.sagemaker.model.Tag> existingTags =
                new HashSet<>(proxyClient.injectCredentialsAndInvokeV2(
                        Translator.translateToListTagsRequest(model), proxyClient.client()::listTags).tags());
        final String imageArn = proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToReadRequest(model), proxyClient.client()::describeImage).imageArn();

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
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToDeleteTagsRequest(tagsToRemove, imageArn),
                    proxyClient.client()::deleteTags);
        }
        if (!tagsToAdd.isEmpty()) {
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToAddTagsRequest(tagsToAdd, imageArn),
                    proxyClient.client()::addTags);
        }
    }
}
