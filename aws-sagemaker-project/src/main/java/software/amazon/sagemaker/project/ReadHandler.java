package software.amazon.sagemaker.project;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeProjectResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ProjectStatus;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

public class ReadHandler extends BaseHandlerStd {

    private Logger logger;
    private ProxyClient<SageMakerClient> proxyClient;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<SageMakerClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        this.proxyClient = proxyClient;

        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-SageMaker-Project::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest))
                .done(this::constructResourceModelFromResponse);

    }

    /**
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param describeProjectRequest the aws service request to describe project
     * @return describe project response
     */
    private DescribeProjectResponse readResource(
            final DescribeProjectRequest describeProjectRequest) {
        DescribeProjectResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(describeProjectRequest, proxyClient.client()::describeProject);
            if (response.projectStatus().equals(ProjectStatus.DELETE_COMPLETED)) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, describeProjectRequest.projectName());
            }
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, describeProjectRequest.projectName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME, describeProjectRequest.projectName(), e);
        }
        return response;
    }

    /**
     * Construction of resource model from the read response, add tags to model
     *
     * @param awsResponse the aws service describe project response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeProjectResponse awsResponse) {
        ResourceModel model = Translator.translateFromReadResponse(awsResponse);
        addTagsToModel(model);
        return ProgressEvent.defaultSuccessHandler(model);
    }

    /**
     * Add tags to the model after fetching it
     *
     * @param model the resource model to which tags have to be added
     */
    private void addTagsToModel(ResourceModel model) {
        ListTagsResponse tagsResponse = proxyClient.injectCredentialsAndInvokeV2(Translator.translateToListTagsRequest(model), proxyClient.client()::listTags);
            List<Tag> tags = Translator.sdkTagsToCfnTags(tagsResponse.tags());
            model.setTags(tags);
    }
}
