package software.amazon.sagemaker.image;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateImageRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateImageResponse;
import software.amazon.awssdk.services.sagemaker.model.ImageStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when creating a new AWS::SageMaker::Image resource.
 */
public class CreateHandler extends BaseHandlerStd {

    private Logger logger;

    @Override
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
                proxy.initiate("AWS-SageMaker-Image::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall((createRequest, prxyClient) -> checkIfExistingAndCreate(model, createRequest, prxyClient))
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
        return readOnlyPropertiesSet;
    }

    /**
     * First checks if the target resource exists already. Afterwards, invokes the create request
     * using the provided proxyClient.
     * @param model the CloudFormation resource model
     * @param createImageRequest the aws service request to create an image
     * @param proxyClient the aws client used to make service calls
     * @return createImageResponse aws service response from creating an image resource
     */
    private CreateImageResponse checkIfExistingAndCreate(
            final ResourceModel model,
            final CreateImageRequest createImageRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        final CreateImageResponse response;
        try {
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                    proxyClient.client()::describeImage);
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, createImageRequest.imageName());
        } catch (final ResourceNotFoundException e) {
            logger.log(String.format("No existing %s [%s] found. Proceeding to create.", ResourceModel.TYPE_NAME,
                    createImageRequest.imageName()));
        }
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(createImageRequest, proxyClient.client()::createImage);
        } catch (final ResourceInUseException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, createImageRequest.imageName(), e);
        } catch (final AwsServiceException e) {
            throw ExceptionMapper.getCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME,
                    createImageRequest.imageName(), e);
        }
        return response;
    }

    /**
     * Stabilization method to ensure that a newly created image resource has moved from CREATING status to CREATED.
     * @param createImageRequest the aws service request to create an image
     * @param createImageResponse the aws service response from creating an image resource
     * @param proxyClient the aws client used to make service calls
     * @param model the CloudFormation resource model
     * @param callbackContext the callback context
     * @return boolean state of whether the image resource has stabilized or not
     */
    private boolean stabilizedOnCreate(
            final CreateImageRequest createImageRequest,
            final CreateImageResponse createImageResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if (model.getImageArn() == null) {
            model.setImageArn(createImageResponse.imageArn());
        }

        final ImageStatus imageStatus= proxyClient.injectCredentialsAndInvokeV2(
                Translator.translateToReadRequest(model), proxyClient.client()::describeImage).imageStatus();

        switch (imageStatus) {
            case CREATE_FAILED:
                throw new CfnGeneralServiceException(String.format("%s [%s] failed to create.", ResourceModel.TYPE_NAME,
                        model.getImageArn()));
            case CREATED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getImageArn(), imageStatus));
                return true;
            case CREATING:
                logger.log(String.format("%s [%s] is stabilizing %s.", ResourceModel.TYPE_NAME,
                        model.getImageArn(), imageStatus));
                return false;
            default:
                throw new CfnGeneralServiceException(
                        String.format("Stabilizing of %s failed with an unexpected status %s",
                                model.getImageArn(), imageStatus));
        }
    }
}
