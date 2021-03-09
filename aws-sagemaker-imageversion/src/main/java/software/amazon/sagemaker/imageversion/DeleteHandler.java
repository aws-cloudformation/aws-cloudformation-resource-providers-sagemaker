package software.amazon.sagemaker.imageversion;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageVersionStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when deleting an AWS::SageMaker::ImageVersion resource.
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
                proxy.initiate("AWS-SageMaker-ImageVersion::Delete", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall((deleteImageVersionRequest, prxyClient) -> deleteImageVersion(model, deleteImageVersionRequest, prxyClient))
                    .stabilize(this::stabilizedOnDelete)
                    .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(OperationStatus.SUCCESS)
                            .build()));
    }

    /**
     * Checks that the image version is not already deleting and invokes the delete request using the provided proxyClient.
     * @param model the CloudFormation resource model
     * @param deleteImageVersionRequest the aws service request to delete an image version
     * @param proxyClient the aws client used to make service calls
     * @return deleteImageVersionResponse aws service response from deleting an image version resource
     */
    private DeleteImageVersionResponse deleteImageVersion(
            final ResourceModel model,
            final DeleteImageVersionRequest deleteImageVersionRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final DeleteImageVersionResponse response;
        try {
            final ImageVersionStatus currentStatus = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToReadRequest(model), proxyClient.client()::describeImageVersion)
                    .imageVersionStatus();
            if (currentStatus.equals(ImageVersionStatus.DELETING)) {
                response = DeleteImageVersionResponse.builder().build();
                return response;
            }
            response = proxyClient.injectCredentialsAndInvokeV2(deleteImageVersionRequest,
                    proxyClient.client()::deleteImageVersion);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getImageVersionArn(), e);
        } catch (final ResourceInUseException e) {
            throw new CfnResourceConflictException(ResourceModel.TYPE_NAME, deleteImageVersionRequest.imageName(),
                    String.format("Image: [%s] is in use. Could not create new Image Version.",
                            deleteImageVersionRequest.imageName()), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME,
                    model.getImageVersionArn(), e);
        }
        return response;
    }

    /**
     * Stabilization method to ensure that a recently delete image version resource no longer exists.
     * @param deleteImageVersionRequest the aws service request to delete an image version
     * @param deleteImageVersionResponse the aws service response from deleting an image version resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the image version resource has stabilized after deletion (no longer found)
     */
    private boolean stabilizedOnDelete(
            final DeleteImageVersionRequest deleteImageVersionRequest,
            final DeleteImageVersionResponse deleteImageVersionResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        final ImageVersionStatus imageVersionStatus;
        try {
            imageVersionStatus = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToReadRequest(model), proxyClient.client()::describeImageVersion)
                    .imageVersionStatus();
        } catch (final ResourceNotFoundException e) {
            return true;
        }

        switch (imageVersionStatus) {
            case DELETING:
                logger.log(String.format("%s with name [%s] is stabilizing while delete.",
                        ResourceModel.TYPE_NAME, model.getImageName()));
                return false;
            case DELETE_FAILED:
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getImageVersionArn());
            default:
                throw new CfnGeneralServiceException(String.format(
                        "Unexpected status: [%s] while stabilizing delete for resource: [%s]",
                        imageVersionStatus, model.getImageVersionArn()));
        }
    }
}
