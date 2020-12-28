package software.amazon.sagemaker.featuregroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.FeatureGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-FeatureGroup::Delete";

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
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .stabilize(this::stabilizedOnDelete)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     * @param awsRequest the aws service request to delete a resource
     * @param proxyClient the aws service client to make the call
     * @return delete resource response
     */
    private DeleteFeatureGroupResponse deleteResource(
            final DeleteFeatureGroupRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteFeatureGroupResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteFeatureGroup);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME,
                    awsRequest.featureGroupName(), e);
        }

        return response;
    }

    /**
     * Stabilization is required to ensure FeatureGroup resource deletion has been completed.
     * @param awsRequest the aws service request to delete a resource
     * @param awsResponse the aws service response to delete a resource
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnDelete(
            final DeleteFeatureGroupRequest awsRequest,
            final DeleteFeatureGroupResponse awsResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        if (model.getFeatureGroupName() == null) {
            model.setFeatureGroupName(awsRequest.featureGroupName());
        }

        try {
            final FeatureGroupStatus featureGroupStatus =
                    proxyClient.injectCredentialsAndInvokeV2(TranslatorForRequest.translateToReadRequest(model),
                    proxyClient.client()::describeFeatureGroup).featureGroupStatus();

            switch (featureGroupStatus) {
                case DELETING:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.",
                            ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                    return false;
                default:
                    throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getFeatureGroupName());
            }
        } catch (ResourceNotFoundException e) {
            return true;
        }
    }
}