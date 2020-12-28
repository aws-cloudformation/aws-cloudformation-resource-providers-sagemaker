package software.amazon.sagemaker.modelpackagegroup;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageGroupRequest;
import software.amazon.awssdk.services.sagemaker.model.GetModelPackageGroupPolicyResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
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

        return proxy.initiate("AWS-SageMaker-ModelPackageGroup::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest))
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param describeModelPackageGroupRequest the aws service request to describe model package group
     * @return describe model package group response
     */
    private DescribeModelPackageGroupResponse readResource(
            final DescribeModelPackageGroupRequest describeModelPackageGroupRequest) {
        DescribeModelPackageGroupResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(describeModelPackageGroupRequest, proxyClient.client()::describeModelPackageGroup);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, describeModelPackageGroupRequest.modelPackageGroupName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME, describeModelPackageGroupRequest.modelPackageGroupName(), e);
        }
        return response;
    }

    /**
     * Construction of resource model from the read response, add resource policy and tags to model
     *
     * @param awsResponse the aws service describe model package group response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeModelPackageGroupResponse awsResponse) {
        ResourceModel model = Translator.translateFromReadResponse(awsResponse);
        addResourcePolicyToModel(model);
        addTagsToModel(model);
        return ProgressEvent.defaultSuccessHandler(model);
    }

    /**
     * Add resource policy to the model after fetching it
     *
     * @param model the resource model to which policy has to be added
     */
    private void addResourcePolicyToModel(ResourceModel model) {
        try {
            GetModelPackageGroupPolicyResponse response = proxyClient.injectCredentialsAndInvokeV2(
                    Translator.translateToGetModelPackageGroupPolicyRequest(model), proxyClient.client()::getModelPackageGroupPolicy);
            if (response.resourcePolicy() != null) {
                model.setModelPackageGroupPolicy(response.resourcePolicy());
            }
        } catch (AwsServiceException e) {
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().matches(".*Cannot find resource policy.*")) {
                // there's no policy available, default null value is used
            } else {
                throw e;
            }
        }
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
