package software.amazon.sagemaker.mlflowtrackingserver;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListMlflowTrackingServersRequest;
import software.amazon.awssdk.services.sagemaker.model.ListMlflowTrackingServersResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * CloudFormation resource handler to be invoked when listing AWS::SageMaker::MlflowTrackingServer resources.
 */
public class ListHandler extends BaseHandlerStd {

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

        return proxy.initiate("AWS-SageMaker-MlflowTrackingServer::List", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> Translator.translateToListRequest(request.getNextToken()))
                .makeServiceCall(this::listMlflowTrackingServers)
                .handleError((listRequest, exception, client, resourceModel, errorCallbackContext) -> {
                    this.logger.log(exception.getMessage());
                    final BaseHandlerException cfnException = ExceptionMapper.getCfnException(Action.LIST.toString(), ResourceModel.TYPE_NAME,
                            "ListMlflowTrackingServers", (AwsServiceException) exception);
                    // Return ProgressEvent with status FAILED and errorCode cfnException.getErrorCode().
                    return ProgressEvent.defaultFailureHandler(cfnException, cfnException.getErrorCode());
                })
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Invokes the list mlflow tracking servers request using the provided proxyClient.
     * @param listMlflowTrackingServersRequest the aws service request to list mlflow tracking servers
     * @param proxyClient the aws client used to make service calls
     * @return listMlflowTrackingServersResponse aws service response from listing mlflow tracking server resources
     */
    private ListMlflowTrackingServersResponse listMlflowTrackingServers(
            final ListMlflowTrackingServersRequest listMlflowTrackingServersRequest,
            final ProxyClient<SageMakerClient> proxyClient) {
            return proxyClient.injectCredentialsAndInvokeV2(listMlflowTrackingServersRequest,
                    proxyClient.client()::listMlflowTrackingServers);
    }

    /**
     * Translates a list mlflow tracking servers response into the CFN resource model format and updates the model.
     * @param listMlflowTrackingServersResponse the aws service response from list mlflow tracking servers request
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final ListMlflowTrackingServersResponse listMlflowTrackingServersResponse) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .nextToken(listMlflowTrackingServersResponse.nextToken())
                .resourceModels(Translator.translateFromListResponse(listMlflowTrackingServersResponse))
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
