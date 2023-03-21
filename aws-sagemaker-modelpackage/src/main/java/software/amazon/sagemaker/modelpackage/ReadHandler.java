package software.amazon.sagemaker.modelpackage;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageResponse;
import software.amazon.awssdk.services.sagemaker.model.DescribeModelPackageRequest;
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

        return proxy.initiate("AWS-SageMaker-ModelPackage::Read", proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest))
            .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param describeModelPackageRequest the aws service request to describe model package
     * @return describe ModelPackage response
     */
    private DescribeModelPackageResponse readResource(
        final DescribeModelPackageRequest describeModelPackageRequest) {
        DescribeModelPackageResponse response = null;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(describeModelPackageRequest, proxyClient.client()::describeModelPackage);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, describeModelPackageRequest.modelPackageName(), e);
        } catch (final AwsServiceException e) {
            ExceptionMapper.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME, describeModelPackageRequest.modelPackageName(), e);
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
        final DescribeModelPackageResponse awsResponse) {
        ResourceModel model = Translator.translateFromReadResponse(awsResponse);

        // Model Package name would be null in response in case of versioned model packages
        if (model.getModelPackageName() != null)
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
