package software.amazon.sagemaker.pipeline;

import com.amazonaws.AmazonServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

public class S3ClientWrapper {

    private static final S3Client s3Client = S3Client.builder().build();

    public static String getBodyFromS3(
            S3Location s3Location,
            AmazonWebServicesClientProxy proxy,
            Logger logger
    ) {
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(s3Location.getBucket())
                    .ifMatch(s3Location.getETag()).versionId(s3Location.getVersion())
                    .key(s3Location.getKey()).build();
            logger.log("Fetching file from S3 with request:" + request.toString());

            String response = proxy.injectCredentialsAndInvokeV2Bytes(request, s3Client::getObjectAsBytes).asUtf8String();
            return response;
        } catch (NoSuchKeyException nske) {
            String errorMsg = String.format("No such key %s/%s with version: %s",
                    s3Location.getBucket(), s3Location.getKey(), s3Location.getVersion());
            logger.log(errorMsg);
            throw new CfnInvalidRequestException(errorMsg);
        } catch (S3Exception | AmazonServiceException se) {
            logger.log("Error while fetching file from S3 " + se.getMessage());
            throw new CfnGeneralServiceException(se.getMessage(), se);
        }
    }
}
