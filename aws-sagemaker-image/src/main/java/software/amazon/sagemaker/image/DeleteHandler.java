package software.amazon.sagemaker.image;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when deleting an existing AWS::SageMaker::Image resource.
 */
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

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-SageMaker-Image::Delete", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall((deleteImageRequest, prxyClient) -> deleteImage(model, deleteImageRequest, prxyClient))
                    .stabilize(this::stabilizedOnDelete)
                    .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(OperationStatus.SUCCESS)
                            .build()));
    }

    /**
     * Checks that the image is not already deleting and invokes the delete request using the provided proxyClient.
     * @param model the CloudFormation resource model
     * @param deleteImageRequest the aws service request to delete an image
     * @param proxyClient the aws client used to make service calls
     * @return deleteImageResponse aws service response from deleting an image resource
     */
    private DeleteImageResponse deleteImage(
            final ResourceModel model,
            final DeleteImageRequest deleteImageRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final DeleteImageResponse response;
        try {
            final ImageStatus currentStatus = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToReadRequest(model), proxyClient.client()::describeImage).imageStatus();
            if (currentStatus.equals(ImageStatus.DELETING)) {
                response = DeleteImageResponse.builder().build();
                return response;
            }
            response = proxyClient.injectCredentialsAndInvokeV2(deleteImageRequest, proxyClient.client()::deleteImage);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteImageRequest.imageName(), e);
        } catch (final ResourceInUseException e) {
            throw new CfnGeneralServiceException(String.format("Image resource %s is in use.",
                    deleteImageRequest.imageName()), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME,
                    deleteImageRequest.imageName(), e);
        }
        return response;
    }

    /**
     * Stabilization method to ensure that a recently delete image resource no longer exists.
     * @param deleteImageRequest the aws service request to delete an image
     * @param deleteImageResponse the aws service response from deleting an image resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the image resource has stabilized after deletion (no longer found)
     */
    private boolean stabilizedOnDelete(
            final DeleteImageRequest deleteImageRequest,
            final DeleteImageResponse deleteImageResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        final ImageStatus imageStatus;
        try {
            imageStatus = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToReadRequest(model), proxyClient.client()::describeImage).imageStatus();
        } catch (final ResourceNotFoundException e) {
            return true;
        }

        switch (imageStatus) {
            case DELETING:
                logger.log(String.format("%s with name [%s] is stabilizing while delete.",
                        ResourceModel.TYPE_NAME, model.getImageName()));
                return false;
            case DELETE_FAILED:
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getImageName());
            default:
                throw new CfnGeneralServiceException(String.format(
                        "Unexpected status: [%s] while stabilizing delete for resource: [%s]",
                        imageStatus, model.getImageArn()));
        }
    }
}
