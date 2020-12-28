package software.amazon.sagemaker.project;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;

import software.amazon.awssdk.services.sagemaker.model.CreateProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceInUseException;
import software.amazon.awssdk.services.sagemaker.model.ProjectStatus;

import software.amazon.cloudformation.Action;

import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.ResourceAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SageMaker-Project::Create", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::createProject)
                                .stabilize(this::stabilizedOnCreate)
                                .progress()
                )
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));

    }

    /**
     * Client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, region and retry settings
     * @param createRequest the aws service request to create a project
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create project resource response
     */
    private CreateProjectResponse createProject(
            final CreateProjectRequest createRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
        CreateProjectResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    createRequest, proxyClient.client()::createProject);
        } catch (final ResourceInUseException e) {
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, createRequest.projectName());
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.CREATE.toString(), ResourceModel.TYPE_NAME, createRequest.projectName(), e);
        }
        return response;
    }

    /**
     * This is used to ensure project resource has moved from Pending to Scheduled/Failed state.
     * @param createRequest the aws service request to create a project
     * @param createResponse the aws service response on creating a project
     * @param proxyClient the aws service client to make the call
     * @param model the resource model
     * @param callbackContext the call back context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnCreate(
            final CreateProjectRequest createRequest,
            final CreateProjectResponse createResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        if(model.getProjectArn() == null){
            model.setProjectArn(createResponse.projectArn());
        }

        final DescribeProjectResponse response = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                proxyClient.client()::describeProject);

        final ProjectStatus projectStatus = response.projectStatus();

        switch (projectStatus) {
            case CREATE_COMPLETED:
            case CREATE_FAILED:
            case DELETE_FAILED:
                logger.log(String.format("%s [%s] has been stabilized with status %s.", ResourceModel.TYPE_NAME,
                        model.getPrimaryIdentifier(), projectStatus));
                return true;

            case PENDING:
            case CREATE_IN_PROGRESS:
            case DELETE_IN_PROGRESS:
                logger.log(String.format("%s [%s] is stabilizing.", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
                return false;
            default:
                throw new CfnGeneralServiceException(
                        "Stabilizing of " + model.getPrimaryIdentifier()
                                + " failed with unexpected status " + projectStatus);
        }
    }



}
