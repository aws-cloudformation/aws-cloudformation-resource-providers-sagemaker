package software.amazon.sagemaker.inferenceexperiment;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeInferenceExperimentResponse;
import software.amazon.awssdk.services.sagemaker.model.InferenceExperimentStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentRequest;
import software.amazon.awssdk.services.sagemaker.model.StopInferenceExperimentResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
        final DescribeInferenceExperimentResponse describeResponse = readResource(
                TranslatorForRequest.translateToReadRequest(model), proxyClient);

        if (model.getArn() == null && describeResponse != null) {
            model.setArn(describeResponse.arn());
        }

        // Stop the in-progress experiment if current status is RUNNING, before calling delete request
        if (describeResponse != null && InferenceExperimentStatus.RUNNING.equals(describeResponse.status())) {
            return ProgressEvent.progress(model, callbackContext)
                    .then(progress ->
                            proxy.initiate("AWS-SageMaker-InferenceExperiment::StopThenDelete", proxyClient, model, callbackContext)
                                    .translateToServiceRequest(TranslatorForRequest::translateToStopRequest)
                                    .makeServiceCall(this::stopResource)
                                    .stabilize(this::stabilizedOnStop)
                                    .progress())
                    .then(progress -> deleteResourceAfterStop(proxyClient, model));
        }

        // Wait for in-progress stop operation to complete before deletion
        if (describeResponse != null && InferenceExperimentStatus.STOPPING.equals(describeResponse.status())) {
            return ProgressEvent.progress(model, callbackContext)
                    .then(progress ->
                            proxy.initiate("AWS-SageMaker-InferenceExperiment::WaitStopThenDelete", proxyClient, model, callbackContext)
                                    .translateToServiceRequest(TranslatorForRequest::translateToReadRequest)
                                    .makeServiceCall(this::readResource)
                                    .stabilize(this::stabilizedOnStop)
                                    .progress())
                    .then(progress -> deleteResourceAfterStop(proxyClient, model));
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SageMaker-InferenceExperiment::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
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
     * Implement client invocation of the delete request through the proxyClient.
     * @param deleteInferenceExperimentRequest the aws service request to delete a resource
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private DeleteInferenceExperimentResponse deleteResource(
            final DeleteInferenceExperimentRequest deleteInferenceExperimentRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteInferenceExperimentResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(deleteInferenceExperimentRequest, proxyClient.client()::deleteInferenceExperiment);
        } catch (ResourceNotFoundException e) {
            // NotFound responded from Delete handler will be considered as success by CFN backend service.
            // This is to handle out of stack resource deletion (https://sage.amazon.com/questions/896677)
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteInferenceExperimentRequest.name());
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(String.format("Failure reason: %s", e.getMessage()), e);
        }

        return response;
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private ProgressEvent<ResourceModel, CallbackContext> deleteResourceAfterStop(
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {

        deleteResource(TranslatorForRequest.translateToDeleteRequest(model), proxyClient);

        return ProgressEvent.defaultSuccessHandler(model);
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
                TranslatorForRequest.updateDefaultModelVariantActionToStopRequest(describeResponse);
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
}