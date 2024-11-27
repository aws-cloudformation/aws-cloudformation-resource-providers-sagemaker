package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.CreateModelPackageRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.ModelPackageStatus;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;

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
        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-SageMaker-ModelPackage::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall(this::createModelPackage)
                    .stabilize(this::stabilizedOnCreate)
                    .done(this::checkAndReturnCreateStatus)
            )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkAndReturnCreateStatus(
        final CreateModelPackageRequest createModelPackageRequest,
        final CreateModelPackageResponse createModelPackageResponse,
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model, final CallbackContext callbackContext) {

        DescribeModelPackageResponse response;
        ProgressEvent<ResourceModel, CallbackContext> progressEvent;
        progressEvent = ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.IN_PROGRESS)
            .build();
        try {
             response = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                proxyClient.client()::describeModelPackage);
            if (response.modelPackageStatus() == ModelPackageStatus.FAILED) {
                String message = String.format("Model Package [%s] status post stabilizing: [%s] due to %s.", response.modelPackageArn(),
                    response.modelPackageStatus().toString(),response.modelPackageStatusDetails().toString());
                logger.log(message);
                ExceptionMapper.throwCfnException(message);
            }
        } catch (final SageMakerException e) {
            ExceptionMapper.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createModelPackageRequest.modelPackageName(), e);
        }
        return progressEvent;
    }

    /**
     * Client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param createModelPackageRequest the aws service request to create a model package
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create model package resource response
     */
    private CreateModelPackageResponse createModelPackage(
        final CreateModelPackageRequest createModelPackageRequest,
        final ProxyClient<SageMakerClient> proxyClient) {
        CreateModelPackageResponse response = null;
        logger.log(String.format("Creating model package with %s", createModelPackageRequest.toString()));
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                createModelPackageRequest, proxyClient.client()::createModelPackage);
        } catch (final ResourceInUseException e) {
            // Possible only for unversioned model packages
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, createModelPackageRequest.modelPackageName());
        } catch (final AwsServiceException e) {
            if ( createModelPackageRequest.modelPackageName() != null ) {
                ExceptionMapper.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createModelPackageRequest.modelPackageName(), e);
            }
            else {
                ExceptionMapper.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createModelPackageRequest.modelPackageGroupName(), e);
            }
        }
        return response;
    }

    /**
     * This is used to ensure model package resource has moved from Pending to Scheduled/Failed state.
     * @param createRequest the aws service request to create a project
     * @param createResponse the aws service response on creating a project
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnCreate(
        final CreateModelPackageRequest createRequest,
        final CreateModelPackageResponse createResponse,
        final ProxyClient<SageMakerClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext) {
        if(model.getModelPackageArn() == null){
            model.setModelPackageArn(createResponse.modelPackageArn());
        }

        final DescribeModelPackageResponse response = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
            proxyClient.client()::describeModelPackage);
        logger.log(String.format("Created Model Package: %s. Stabilizing...", response.toString()));

        final ModelPackageStatus modelPackageStatus = response.modelPackageStatus();
        switch (modelPackageStatus) {
            case COMPLETED:
            case FAILED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                    model.getPrimaryIdentifier(), modelPackageStatus));
                return true;

            case PENDING:
            case IN_PROGRESS:
            case DELETING:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException(
                    "Stabilizing of " + model.getPrimaryIdentifier()
                        + " failed with unexpected status " + modelPackageStatus);
        }
    }
}
