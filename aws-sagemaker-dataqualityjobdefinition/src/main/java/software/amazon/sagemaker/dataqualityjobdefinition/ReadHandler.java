package software.amazon.sagemaker.dataqualityjobdefinition;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DescribeDataQualityJobDefinitionRequest;
import software.amazon.awssdk.services.sagemaker.model.DescribeDataQualityJobDefinitionResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<SageMakerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        //Set job definition name if absent
        String jobDefinitionName = model.getJobDefinitionName();
        if(StringUtils.isEmpty(jobDefinitionName)){
            jobDefinitionName = Utils.getResourceNameFromArn(model.getJobDefinitionArn(), DATA_QUALITY_ARN_SUBSTRING);
            model.setJobDefinitionName(jobDefinitionName);
        }

        return proxy.initiate("AWS-SageMaker-DataQualityJobDefinition::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(TranslatorForRequest::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest, sdkProxyClient, model))
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeDataQualityJobDefinitionResponse readResource(
            final DescribeDataQualityJobDefinitionRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient,
            final ResourceModel model) {

        DescribeDataQualityJobDefinitionResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeDataQualityJobDefinition);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.jobDefinitionName(), e);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.READ.toString(), e);
        }

        return response;
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already
     * initialised with caller credentials, correct region and retry settings
     *
     * @param awsResponse the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeDataQualityJobDefinitionResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(TranslatorForResponse.translateFromReadResponse(awsResponse));
    }
}
