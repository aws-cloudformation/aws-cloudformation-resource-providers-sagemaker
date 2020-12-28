package software.amazon.sagemaker.featuregroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateFeatureGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateFeatureGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.FeatureGroupStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-FeatureGroup::Create";

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
                                .translateToServiceRequest(TranslatorForRequest::translateToCreateRequest)
                                .makeServiceCall(this::createResource)
                                .stabilize(this::stabilizedOnCreate)
                                .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param awsRequest the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create resource response
     */
    private CreateFeatureGroupResponse createResource(
            final CreateFeatureGroupRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        CreateFeatureGroupResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createFeatureGroup);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME,
                    awsRequest.featureGroupName(), e);
        }
        return response;
    }

    /**
     * This is used to ensure FeatureGroup resource has moved from Creating to Created/Failed state.
     * @param awsRequest the aws service request to create a resource
     * @param awsResponse the aws service response to create a resource
     * @param proxyClient the aws service client to make the call
     * @param model Resource Model
     * @param callbackContext call back context
     * @return boolean flag indicate if the creation is stabilized
     */
    private boolean stabilizedOnCreate(
            final CreateFeatureGroupRequest awsRequest,
            final CreateFeatureGroupResponse awsResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if (model.getFeatureGroupName() == null) {
            model.setFeatureGroupName(awsRequest.featureGroupName());
        }

        final FeatureGroupStatus featureGroupStatus;
        try {
            featureGroupStatus = proxyClient.injectCredentialsAndInvokeV2(
                    TranslatorForRequest.translateToReadRequest(model),
                    proxyClient.client()::describeFeatureGroup).featureGroupStatus();
        } catch (ResourceNotFoundException rnfe) {
            logger.log(String.format("Resource not found for %s, stabilizing.", model.getPrimaryIdentifier()));
            return false;
        }

        switch (featureGroupStatus) {
            case CREATED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), featureGroupStatus));
                return true;
            case CREATING:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier()));
                return false;
            default:
                logger.log(String.format("%s [%s] failed to stabilize with status: %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), featureGroupStatus));
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getFeatureGroupName());
        }
    }
}
