package software.amazon.sagemaker.domain;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateDomainRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeDomainResponse;
import software.amazon.awssdk.services.sagemaker.model.DomainStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Domain::Create";
    private static final String READ_ONLY_PROPERTY_ERROR_MESSAGE = "The following property '%s' is not allowed to configured.";

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        // read only properties are not allowed to be set by the user during creation.
        // https://github.com/aws-cloudformation/aws-cloudformation-resource-schema/issues/102
        if (callbackContext.callGraphs().isEmpty()) {
            if (model.getDomainArn() != null) {
                throw new CfnInvalidRequestException(String.format(READ_ONLY_PROPERTY_ERROR_MESSAGE, "DomainArn"));
            }

            if (model.getDomainId() != null) {
                throw new CfnInvalidRequestException(String.format(READ_ONLY_PROPERTY_ERROR_MESSAGE, "DomainId"));
            }

            if (model.getUrl() != null) {
                throw new CfnInvalidRequestException(String.format(READ_ONLY_PROPERTY_ERROR_MESSAGE, "Url"));
            }

            if (model.getHomeEfsFileSystemId() != null) {
                throw new CfnInvalidRequestException(String.format(READ_ONLY_PROPERTY_ERROR_MESSAGE, "HomeEfsFileSystemId"));
            }

            if (model.getSingleSignOnManagedApplicationInstanceId() != null) {
                throw new CfnInvalidRequestException(String.format(READ_ONLY_PROPERTY_ERROR_MESSAGE, "SingleSignOnManagedApplicationInstanceId"));
            }
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                        .translateToServiceRequest(TranslatorForRequest::translateToCreateRequest)
                        .makeServiceCall(this::createResource)
                        .stabilize(this::stabilizedOnCreate)
                        .done(createResponse -> constructResourceModelFromResponse(createResponse, model, proxyClient))
                );
    }

    /**
     * Client invocation of the create request through the proxyClient
     *
     * @param createRequest aws service create resource request
     * @param proxyClient   aws service client to make the call
     * @return create resource response
     */
    private CreateDomainResponse createResource(
            final CreateDomainRequest createRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        CreateDomainResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    createRequest, proxyClient.client()::createDomain);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createRequest.domainName(), e);
        }

        // Wait 5 seconds for Domain to show up in List and Describe calls.
        try {
            Thread.sleep(5*1000);
        } catch(InterruptedException ignored) {
        }

        return response;
    }

    /**
     * Ensure resource has moved from pending to terminal state.
     *
     * @param awsResponse the aws service response to create a resource
     * @param proxyClient the aws service client to make the call
     * @param model Resource Model
     * @param callbackContext call back context
     * @return boolean flag indicate if the creation is stabilized
     */
    private boolean stabilizedOnCreate(
            final CreateDomainRequest awsRequest,
            final CreateDomainResponse awsResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        if (model.getDomainId() == null) {
            model.setDomainId(getDomainIdFromArn(awsResponse.domainArn()));
        }

        final DomainStatus DomainStatus;
        try {
            DomainStatus = proxyClient.injectCredentialsAndInvokeV2(
                    TranslatorForRequest.translateToReadRequest(model),
                    proxyClient.client()::describeDomain).status();
        } catch (ResourceNotFoundException rnfe) {
            logger.log(String.format("Resource not found for %s, stabilizing.", model.getPrimaryIdentifier()));
            return false;
        }

        switch (DomainStatus) {
            case IN_SERVICE:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), DomainStatus));
                return true;
            case PENDING:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier()));
                return false;
            default:
                logger.log(String.format("%s [%s] failed to stabilize with status: %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), DomainStatus));
                throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getDomainName());
        }
    }

    /**
     * Build the Progress Event object from the describe response.
     *
     * @param awsResponse the aws service response to create a resource
     * @param model resource model
     * @param proxyClient the aws service client to make the call
     * @return progressEvent indicating success
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final CreateDomainResponse awsResponse,
            final ResourceModel model,
            final ProxyClient<SageMakerClient> proxyClient) {
        if (model.getDomainId() == null) {
            model.setDomainId(getDomainIdFromArn(awsResponse.domainArn()));
        }

        DescribeDomainResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    TranslatorForRequest.translateToReadRequest(model),
                    proxyClient.client()::describeDomain);
        } catch (ResourceNotFoundException e) {
            Translator.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString(), e);
        }

        model.setDomainArn(response.domainArn());
        model.setUrl(response.url());
        model.setHomeEfsFileSystemId(response.homeEfsFileSystemId());
        model.setSingleSignOnManagedApplicationInstanceId(response.singleSignOnManagedApplicationInstanceId());

        return ProgressEvent.defaultSuccessHandler(model);
    }

    private static String getDomainIdFromArn(final String arn) {
        String[] splitArn = arn.split("/");
        return splitArn[splitArn.length - 1];
    }
}
