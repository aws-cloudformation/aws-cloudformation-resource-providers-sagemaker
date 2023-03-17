package software.amazon.sagemaker.inferenceexperiment;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

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

        List<Tag> allTags = TagHelper.consolidateResourceTags(model, request);
        model.setTags(allTags);

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                    proxy.initiate("AWS-SageMaker-InferenceExperiment::Create", proxyClient, model, callbackContext)
                        .translateToServiceRequest(TranslatorForRequest::translateToCreateRequest)
                        .makeServiceCall(this::createResource)
                        .stabilize(this::stabilizedOnCreate)
                        .progress())
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param request the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create resource response
     */
    private CreateInferenceExperimentResponse createResource(
            final CreateInferenceExperimentRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        CreateInferenceExperimentResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::createInferenceExperiment);
        } catch (final ResourceInUseException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, request.name());
        } catch (final AwsServiceException e) {
            // The exception thrown due to validation failure does not have error code set,
            // hence we need to check it using error message
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("validation error detected")) {
                throw new CfnInvalidRequestException(String.format("Failure reason: %s", e.getMessage()), e);
            }
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * This is used to ensure InferenceExperiment resource has moved from Creating to Created/Running state.
     * @param request the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnCreate(
        final CreateInferenceExperimentRequest request,
        final CreateInferenceExperimentResponse response,
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext) {

        if (model.getArn() == null && response != null) {
            model.setArn(response.inferenceExperimentArn());
        }

        final InferenceExperimentStatus status = proxyClient.injectCredentialsAndInvokeV2(TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeInferenceExperiment).status();

        switch (status) {
            case CREATED:
            case RUNNING:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), status));
                return true;
            case CREATING:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing of " + model.getPrimaryIdentifier());

        }
    }

}