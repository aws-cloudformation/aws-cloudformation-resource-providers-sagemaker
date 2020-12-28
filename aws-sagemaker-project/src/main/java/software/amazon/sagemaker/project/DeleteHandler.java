package software.amazon.sagemaker.project;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.DeleteProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.ProjectStatus;
import software.amazon.awssdk.services.sagemaker.model.SageMakerException;
import software.amazon.cloudformation.Action;
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

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-SageMaker-Project::Delete", proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .stabilize(this::stabilizedOnDelete)
                                .done(this::checkAndReturnDeleteStatus));

    }

    private ProgressEvent<ResourceModel, CallbackContext> checkAndReturnDeleteStatus(
            final DeleteProjectRequest deleteProjectRequest,
            final DeleteProjectResponse deleteProjectResponse,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model, final CallbackContext callbackContext) {

        OperationStatus delStatus = OperationStatus.SUCCESS;
        try {

            DescribeProjectResponse response = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToReadRequest(model),
                    proxyClient.client()::describeProject);
            delStatus = response.projectStatus() == ProjectStatus.DELETE_COMPLETED ?
                    OperationStatus.SUCCESS : OperationStatus.FAILED;
            logger.log(String.format("%s Project status post stabilizing: [%s].", model.getProjectName(),
                    delStatus.toString()));

        } catch (final ResourceNotFoundException e) {
        } catch (final SageMakerException e) {
            if (false == isExceptionFromDeletedProject(e)) {
                throw e;
            }
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(delStatus)
                .build();
    }

    /**
     * Implement client invocation of the delete request through the proxyClient.
     * @param deleteProjectRequest the aws service request to delete a project
     * @param proxyClient the aws service client to make the call
     * @return delete project response
     */
    private DeleteProjectResponse deleteResource(
            final DeleteProjectRequest deleteProjectRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteProjectResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(deleteProjectRequest, proxyClient.client()::deleteProject);
        } catch (ResourceNotFoundException e) {
            // NotFound responded from Delete handler will be considered as success by CFN backend service.
            // This is to handle out of stack resource deletion
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteProjectRequest.projectName());
        }  catch (final AwsServiceException e) {
            if (isExceptionFromDeletedProject(e)) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteProjectRequest.projectName());
            }
            ExceptionMapper.throwCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME, deleteProjectRequest.projectName(), e);
        }

        return response;
    }

    /**
     * Sync delete API moves resource in PENDING state and actual deletion happens asynchronously.
     * Stabilization is required to ensure project resource deletion has been completed.
     * @param deleteProjectRequest the aws service request to delete a project
     * @param deleteProjectResult the aws service response on deleting a project
     * @param proxyClient the aws service client to make the call
     * @param model resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnDelete(
            final DeleteProjectRequest deleteProjectRequest,
            final DeleteProjectResponse deleteProjectResult,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        try {
            DescribeProjectResponse response = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                    proxyClient.client()::describeProject);
            final ProjectStatus projectStatus = response.projectStatus();

            switch (projectStatus) {
                case DELETE_IN_PROGRESS:
                case PENDING:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.", ResourceModel.TYPE_NAME, model.getProjectName()));
                    return false;
                //Delete failure case
                case DELETE_FAILED:
                case DELETE_COMPLETED:
                    logger.log(String.format("%s with name [%s] is stabilizing while delete.", ResourceModel.TYPE_NAME, model.getProjectName()));
                    return true;
                default:
                    throw new CfnGeneralServiceException("Delete stabilizing of project: " + model.getProjectName());
            }
        } catch (final ResourceNotFoundException e) {
            return true;
        } catch (final SageMakerException e) {
            if (isExceptionFromDeletedProject(e)) {
                return true;
            }
            throw e;
        }
    }

    private boolean isExceptionFromDeletedProject(final AwsServiceException e) {
        if (StringUtils.isNotBlank(e.getMessage())
                && (e.getMessage().matches(".*Project .* does not exist.*")
                || e.getMessage().matches(".*Project.*in DeleteCompleted status.*"))) {
            return true;
        }
        return false;
    }

}
