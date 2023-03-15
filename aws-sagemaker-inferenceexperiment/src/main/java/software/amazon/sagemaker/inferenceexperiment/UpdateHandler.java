package software.amazon.sagemaker.inferenceexperiment;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.StartInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.StartInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.UpdateInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateInferenceExperimentResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

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
        final DescribeInferenceExperimentResponse describeResponse = readResource(
                TranslatorForRequest.translateToReadRequest(model), proxyClient);

        if (model.getArn() == null && describeResponse != null) {
            model.setArn(describeResponse.arn());
        }

        List<Tag> allTags = TagHelper.consolidateResourceTags(model, request);

        if (describeResponse != null && describeResponse.status() != null) {
            switch (describeResponse.status()) {
                case CREATED:
                    // Start the experiment if the desired state is set to RUNNING and current experiment status is CREATED
                    if (InferenceExperimentStatus.RUNNING.toString().equalsIgnoreCase(model.getDesiredState())) {
                        return ProgressEvent.progress(model, callbackContext)
                                .then(progress ->
                                        proxy.initiate("AWS-SageMaker-InferenceExperiment::Start", proxyClient, model, callbackContext)
                                                .translateToServiceRequest(TranslatorForRequest::translateToStartRequest)
                                                .makeServiceCall(this::startResource)
                                                .stabilize(this::stabilizedOnStart)
                                                .progress())
                                .then(progress -> TagHelper.updateResourceTags(proxyClient, model, allTags, callbackContext))
                                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
                    }
                    // Update the experiment when new experiment config when current experiment status is CREATED
                    return ProgressEvent.progress(model, callbackContext)
                            .then(progress ->
                                    proxy.initiate("AWS-SageMaker-InferenceExperiment::Update", proxyClient, model, callbackContext)
                                            .translateToServiceRequest(TranslatorForRequest::translateToUpdateRequest)
                                            .makeServiceCall(this::updateResource)
                                            .stabilize(this::stabilizedOnUpdate)
                                            .progress())
                            .then(progress -> TagHelper.updateResourceTags(proxyClient, model, allTags, callbackContext))
                            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
                case RUNNING:
                    // Stop the experiment if the desired state is set to COMPLETED/CANCELLED and current experiment status is RUNNING
                    if ((InferenceExperimentStatus.COMPLETED.toString().equalsIgnoreCase(model.getDesiredState())
                            || InferenceExperimentStatus.CANCELLED.toString().equalsIgnoreCase(model.getDesiredState()))) {
                        return ProgressEvent.progress(model, callbackContext)
                                .then(progress ->
                                        proxy.initiate("AWS-SageMaker-InferenceExperiment::Stop", proxyClient, model, callbackContext)
                                                .translateToServiceRequest(TranslatorForRequest::translateToStopRequest)
                                                .makeServiceCall(this::stopResource)
                                                .stabilize(this::stabilizedOnStop)
                                                .progress())
                                .then(progress -> TagHelper.updateResourceTags(proxyClient, model, allTags, callbackContext))
                                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
                    }
                    // Update the experiment when new experiment runtime config when current experiment status is RUNNING
                    return ProgressEvent.progress(model, callbackContext)
                            .then(progress ->
                                    proxy.initiate("AWS-SageMaker-InferenceExperiment::Update", proxyClient, model, callbackContext)
                                            .translateToServiceRequest(TranslatorForRequest::translateToUpdateRunningResourceRequest)
                                            .makeServiceCall(this::updateResource)
                                            .stabilize(this::stabilizedOnUpdate)
                                            .progress())
                            .then(progress -> TagHelper.updateResourceTags(proxyClient, model, allTags, callbackContext))
                            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
                case UPDATING:
                    // Wait for in-progress update operation to complete before updating
                    return ProgressEvent.progress(model, callbackContext)
                            .then(progress ->
                                    proxy.initiate("AWS-SageMaker-InferenceExperiment::WaitConcurrentUpdate", proxyClient, model, callbackContext)
                                            .translateToServiceRequest(TranslatorForRequest::translateToReadRequest)
                                            .makeServiceCall(this::readResource)
                                            .stabilize(this::stabilizedOnConcurrentUpdate)
                                            .progress())
                            .then(progress -> TagHelper.updateResourceTags(proxyClient, model, allTags, callbackContext))
                            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
               case STOPPING:
                    // Wait for in-progress stop operation to complete before updating
                    return ProgressEvent.progress(model, callbackContext)
                            .then(progress ->
                                    proxy.initiate("AWS-SageMaker-InferenceExperiment::WaitStop", proxyClient, model, callbackContext)
                                            .translateToServiceRequest(TranslatorForRequest::translateToReadRequest)
                                            .makeServiceCall(this::readResource)
                                            .stabilize(this::stabilizedOnStop)
                                            .progress())
                            .then(progress -> TagHelper.updateResourceTags(proxyClient, model, allTags, callbackContext))
                            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
                default:
                    break;
            }
        }

        // For completed/cancelled experiment, handler only updates tags as other properties become immutable
        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> TagHelper.updateResourceTags(proxyClient, model, allTags, callbackContext))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param request the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeInferenceExperimentResponse readResource(
            final DescribeInferenceExperimentRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        DescribeInferenceExperimentResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeInferenceExperiment);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.name(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * Client invocation of the start request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param request the aws service request to start a resource
     * @param proxyClient the aws service client to make the call
     * @return start resource response
     */
    private StartInferenceExperimentResponse startResource(
            final StartInferenceExperimentRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        StartInferenceExperimentResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::startInferenceExperiment);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.name(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * Client invocation of the stop request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param request the aws service request to stop a resource
     * @param proxyClient the aws service client to make the call
     * @return stop resource response
     */
    private StopInferenceExperimentResponse stopResource(
            final StopInferenceExperimentRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        final DescribeInferenceExperimentResponse describeResponse = readResource(
                TranslatorForRequest.translateResourceNameToReadRequest(request.name()), proxyClient);
        final StopInferenceExperimentRequest stopRequest =
                TranslatorForRequest.updateModelVariantActionToStopRequest(request, describeResponse);
        StopInferenceExperimentResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(stopRequest, proxyClient.client()::stopInferenceExperiment);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.name(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * Client invocation of the update request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param request the aws service request to update a resource
     * @param proxyClient the aws service client to make the call
     * @return update resource response
     */
    private UpdateInferenceExperimentResponse updateResource(
            final UpdateInferenceExperimentRequest request,
            final ProxyClient<SageMakerClient> proxyClient) {

        UpdateInferenceExperimentResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::updateInferenceExperiment);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.name(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * This is used to ensure InferenceExperiment resource has updated.
     * @param request the aws service request to update a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnUpdate(
            final UpdateInferenceExperimentRequest request,
            final UpdateInferenceExperimentResponse response,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        final InferenceExperimentStatus status = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeInferenceExperiment).status();

        switch (status) {
            case CREATED:
            case RUNNING:
                logger.log(String.format("%s [%s] has been stabilized with state %s during update operation.",
                        ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), status));
                return true;
            case UPDATING:
                logger.log(String.format("%s [%s] is stabilizing during update.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing during update of " + model.getPrimaryIdentifier());

        }
    }

    /**
     * This is used to ensure InferenceExperiment resource has started.
     * @param request the aws service request to start a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnStart(
            final StartInferenceExperimentRequest request,
            final StartInferenceExperimentResponse response,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        final InferenceExperimentStatus status = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeInferenceExperiment).status();

        switch (status) {
            case RUNNING:
                logger.log(String.format("%s [%s] has been stabilized with state %s during start operation.",
                        ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), status));
                return true;
            case STARTING:
                logger.log(String.format("%s [%s] is stabilizing during start.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing during start of " + model.getPrimaryIdentifier());

        }
    }

    /**
     * This is used to ensure InferenceExperiment resource has stopped.
     * @param request the aws service request to stop a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnStop(
            final StopInferenceExperimentRequest request,
            final StopInferenceExperimentResponse response,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        return isResourceStopped(proxyClient, model);
    }

    /**
     * This is used to ensure InferenceExperiment resource has stopped.
     * @param request the aws service request to stop a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnStop(
            final DescribeInferenceExperimentRequest request,
            final DescribeInferenceExperimentResponse response,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        return isResourceStopped(proxyClient, model);
    }

    /**
     * This is used to ensure InferenceExperiment resource has updated.
     * @param request the aws service request to stop a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnConcurrentUpdate(
            final DescribeInferenceExperimentRequest request,
            final DescribeInferenceExperimentResponse response,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        return isResourceUpdated(proxyClient, model);
    }

    /**
     * This is used to ensure InferenceExperiment resource has stopped.
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     */
    private boolean isResourceStopped(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {

        final InferenceExperimentStatus status = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeInferenceExperiment).status();

        switch (status) {
            case CANCELLED:
            case COMPLETED:
                logger.log(String.format("%s [%s] has been stabilized with state %s during stop operation.",
                        ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), status));
                return true;
            case STOPPING:
                logger.log(String.format("%s [%s] is stabilizing during stop.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing during stop of " + model.getPrimaryIdentifier());

        }
    }

    /**
     * This is used to ensure InferenceExperiment resource has been updated.
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     */
    private boolean isResourceUpdated(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {

        final InferenceExperimentStatus status = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeInferenceExperiment).status();

        switch (status) {
            case CREATED:
            case RUNNING:
                logger.log(String.format("%s [%s] has been stabilized with state %s during stop operation.",
                        ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), status));
                return true;
            case UPDATING:
                logger.log(String.format("%s [%s] is stabilizing during update.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing during update of " + model.getPrimaryIdentifier());

        }
    }
}