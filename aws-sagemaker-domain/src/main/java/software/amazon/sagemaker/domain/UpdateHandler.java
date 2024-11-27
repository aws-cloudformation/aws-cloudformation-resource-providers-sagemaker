package software.amazon.sagemaker.domain;

import java.util.HashSet;
import java.util.Set;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DomainStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.UpdateDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.UpdateDomainResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
public class UpdateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Domain::Update";
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<SageMakerClient> proxyClient,
            Logger logger
    ) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToUpdateRequest)
                                .makeServiceCall((updateDomainRequest, _proxyClient) -> this.updateResource(request, updateDomainRequest, proxyClient))
                                .stabilize(this::stabilizedOnUpdate)
                                .progress())
                .then(progress -> constructResourceModelFromResponse(model, proxyClient));
    }

    /**
     * Client invocation of the update request through the proxyClient
     *
     * @param request resource handler request
     * @param updateDomainRequest aws service update resource request
     * @param proxyClient the aws service client to make the call
     * @return update resource response
     */
    private UpdateDomainResponse updateResource(
            final ResourceHandlerRequest<ResourceModel> request,
            final UpdateDomainRequest updateDomainRequest,
            final ProxyClient<SageMakerClient> proxyClient
    ) {
        UpdateDomainResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(updateDomainRequest, proxyClient.client()::updateDomain);
            updateTags(response.domainArn(), request, proxyClient);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, updateDomainRequest.domainId(), e);
        }
        return response;
    }

    /**
     * This is used to ensure Domain resource has moved from Pending to any terminal state
     * (e.g. Scheduled, Stopped).
     *
     * @param updateDomainRequest the aws service request to update a resource
     * @param proxyClient the aws service client to make the call
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnUpdate(
            final UpdateDomainRequest updateDomainRequest,
            final UpdateDomainResponse updateDomainResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        final DomainStatus domainState = proxyClient.injectCredentialsAndInvokeV2(
                TranslatorForRequest.translateToReadRequest(model),
                proxyClient.client()::describeDomain).status();

        switch (domainState) {
            case IN_SERVICE:
                logger.log(String.format("%s [%s] has been stabilized with state %s during update operation.",
                        ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), domainState));
                return true;
            case UPDATING:
                logger.log(String.format("%s [%s] is stabilizing during update.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException("Stabilizing during update of " + model.getPrimaryIdentifier());

        }
    }

    /**
     * Build the Progress Event object from the describe response.
     *
     * @param model resource model
     * @return progressEvent indicating success
     */
    @SuppressWarnings("resource")
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ResourceModel model,
            final ProxyClient<SageMakerClient> proxyClient) {
        DescribeDomainResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    TranslatorForRequest.translateToReadRequest(model),
                    proxyClient.client()::describeDomain);
        } catch (ResourceNotFoundException e) {
            Translator.throwCfnException(Action.UPDATE.toString(), ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString(), e);
        }

        model.setDomainArn(response.domainArn());

        return ProgressEvent.defaultSuccessHandler(model);
    }

    private void updateTags(String domainArn, ResourceHandlerRequest<ResourceModel> request, ProxyClient<SageMakerClient> proxyClient) {
        Set<Tag> currentUserTags = new HashSet<>();
        Set<Tag> previousTags = new HashSet<>();

        if (request.getDesiredResourceState().getTags() != null) {
            currentUserTags.addAll(request.getDesiredResourceState().getTags());
        }
        currentUserTags.addAll(TaggingHelper.transformTags(request.getDesiredResourceTags()));
        if (request.getPreviousResourceState() != null && request.getPreviousResourceState().getTags() != null) {
            previousTags.addAll(request.getPreviousResourceState().getTags());
        }
        previousTags.addAll(TaggingHelper.transformTags(request.getPreviousResourceTags()));

        TaggingHelper.updateTags(domainArn, previousTags, currentUserTags, proxyClient);
    }
}
