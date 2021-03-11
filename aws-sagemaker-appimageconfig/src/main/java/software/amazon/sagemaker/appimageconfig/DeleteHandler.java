package software.amazon.sagemaker.appimageconfig;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppImageConfigRequest;
import software.amazon.awssdk.services.sagemaker.model.DeleteAppImageConfigResponse;
import software.amazon.awssdk.services.sagemaker.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    private static final String OPERATION = "AWS-SageMaker-AppImageConfig::Delete";

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
                        proxy.initiate(OPERATION, proxyClient, model, callbackContext)
                                .translateToServiceRequest(TranslatorForRequest::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResource)
                                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .status(OperationStatus.SUCCESS)
                                        .build()));
    }

    /**
     * Client invocation of the delete request through the proxyClient.
     *
     * @param awsRequest aws service delete resource request
     * @param proxyClient aws service client to make the call
     * @return delete resource response
     */
    private DeleteAppImageConfigResponse deleteResource(
            final DeleteAppImageConfigRequest awsRequest,
            final ProxyClient<SageMakerClient> proxyClient) {

        DeleteAppImageConfigResponse response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteAppImageConfig);
        } catch (final AwsServiceException e) {
            Translator.throwCfnException(Action.DELETE.toString(), ResourceModel.TYPE_NAME,
                    awsRequest.appImageConfigName(), e);
        }

        return response;
    }
}
