package software.amazon.sagemaker.imageversion;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateImageVersionRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateImageVersionResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageVersionStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when creating a new AWS::SageMaker::ImageVersion resource.
 */
public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        if (callbackContext.callGraphs().isEmpty()) {
            final List<String> readOnlyProperties = getReadOnlyProperties(model);
            if (!readOnlyProperties.isEmpty()) {
                throw new CfnInvalidRequestException(String.format("The following ReadOnly properties were set: [%s]",
                        StringUtils.join(readOnlyProperties,",")));
            }
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-SageMaker-ImageVersion::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(resourceModel -> Translator.translateToCreateRequest(model, request.getClientRequestToken()))
                    .makeServiceCall(this::createImageVersion)
                    .stabilize(this::stabilizedOnCreate)
                    .progress())
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Returns a list of any readOnly properties that are set on the input model.
     * @param model the input CFN resource model
     * @return List of Strings of any readOnly properties set
     */
    private List<String> getReadOnlyProperties(final ResourceModel model) {
        final List<String> readOnlyPropertiesSet = new ArrayList<>();
        if (model.getImageArn() != null) {
            readOnlyPropertiesSet.add("ImageArn");
        }
        if (model.getImageVersionArn() != null) {
            readOnlyPropertiesSet.add("ImageVersionArn");
        }
        if (model.getVersion() != null) {
            readOnlyPropertiesSet.add("Version");
        }
        if (model.getContainerImage() != null) {
            readOnlyPropertiesSet.add("ContainerImage");
        }
        return readOnlyPropertiesSet;
    }

    /**
     * Invokes the create request using the provided proxyClient.
     * @param createImageVersionRequest the aws service request to create an image version
     * @param proxyClient the aws client used to make service calls
     * @return createImageResponse aws service response from creating an image version resource
     */
    private CreateImageVersionResponse createImageVersion(
            final CreateImageVersionRequest createImageVersionRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final CreateImageVersionResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(createImageVersionRequest,
                    proxyClient.client()::createImageVersion);
        } catch (final ResourceInUseException e) {
            throw new CfnResourceConflictException(ResourceModel.TYPE_NAME, createImageVersionRequest.imageName(),
                    String.format("Image: [%s] is in use. Could not create new Image Version.",
                            createImageVersionRequest.imageName()), e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnInvalidRequestException(String.format("Image: [%s] not found. Failed to create Image Version",
                    createImageVersionRequest.imageName()), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, String.format(
                    "ImageVersion for image: %s failed to create.", createImageVersionRequest.imageName()), e);
        }
        return response;
    }

    /**
     * Stabilization method to ensure that a newly created image version resource has moved from CREATING status to CREATED.
     * @param createImageVersionRequest the aws service request to create an image version
     * @param createImageVersionResponse the aws service response from creating an image version resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the image version resource has stabilized or not
     */
    private boolean stabilizedOnCreate(
            final CreateImageVersionRequest createImageVersionRequest,
            final CreateImageVersionResponse createImageVersionResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if (model.getImageVersionArn() == null) {
            model.setImageVersionArn(createImageVersionResponse.imageVersionArn());
        }

        final ImageVersionStatus imageVersionStatus= proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToReadRequest(model), proxyClient.client()::describeImageVersion)
                .imageVersionStatus();

        switch (imageVersionStatus) {
            case CREATE_FAILED:
                throw new CfnGeneralServiceException(String.format("%s [%s] failed to create.", ResourceModel.TYPE_NAME,
                        model.getImageVersionArn()));
            case CREATED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getImageVersionArn(), imageVersionStatus));
                return true;
            case CREATING:
                logger.log(String.format("%s [%s] is stabilizing %s.", ResourceModel.TYPE_NAME,
                        model.getImageVersionArn(), imageVersionStatus));
                return false;
            default:
                throw new CfnGeneralServiceException(
                        String.format("Stabilizing of %s failed with an unexpected status %s",
                                model.getImageVersionArn(), imageVersionStatus));
        }
    }
}
