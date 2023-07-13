package software.amazon.sagemaker.pipeline;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribePipelineResponse;
import software.amazon.awssdk.services.sagemaker.model.ListTagsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListTagsResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.awssdk.utils.Pair;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

public class ReadHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-Pipeline::Read";
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger
    ) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                .translateToServiceRequest(TranslatorForRequest::translateToReadRequest)
                .makeServiceCall(this::invokeDescribePipelineAndListTags)
                .done(this::constructResourceModelFromResponse);
    }

    private Pair<DescribePipelineResponse, List<Tag>> invokeDescribePipelineAndListTags(
            DescribePipelineRequest describePipelineRequest, final ProxyClient<SageMakerClient> proxyClient) {
        DescribePipelineResponse describePipelineResponse =
                describePipelineResponse(describePipelineRequest, proxyClient);
        List<Tag> tags = listTags(ListTagsRequest.builder()
                .resourceArn(describePipelineResponse.pipelineArn())
                .build(), proxyClient);
        return Pair.of(describePipelineResponse, tags);
    }

    /**
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param request the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribePipelineResponse describePipelineResponse(final DescribePipelineRequest request,
                                                              final ProxyClient<SageMakerClient> proxyClient) {
        DescribePipelineResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describePipeline);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.pipelineName(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME, request.pipelineName(), e);
        }
        return response;
    }

    /**
     * Fetch tags from the pipeline resource model and convert into CFN Tag representation
     * @param request list tags request
     * @param proxyClient the aws service client to make the call
     * @return list of CloudFormation Tag objects
     */
    private List<Tag> listTags(final ListTagsRequest request,
                               final ProxyClient<SageMakerClient> proxyClient) {
        List<Tag> tags = null;
        try {
            final ListTagsResponse tagsResponse = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::listTags);
            tags = Translator.sdkTagsToCfnTags(tagsResponse.tags());
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.READ.toString(), ResourceModel.TYPE_NAME, request.resourceArn(), e);
        }
        return tags;
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already
     * initialised with caller credentials, correct region and retry settings
     *
     * @param responses pair of DescribePipelines response and ListTags response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            Pair<DescribePipelineResponse, List<Tag>> responses) {
        final ResourceModel model = TranslatorForResponse.translateFromReadResponse(responses.left());
        final List<Tag> tags = responses.right();
        if (!tags.isEmpty()) {
            model.setTags(tags);
        }
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
