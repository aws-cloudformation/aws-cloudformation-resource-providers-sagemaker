# AWS::SageMaker::Space

Congratulations on starting development! Next steps:

1. Write the JSON schema describing your resource, `aws-sagemaker-space.json`
1. Implement your resource handlers.

The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.

### Run contract-tests locally
https://w.amazon.com/bin/view/AWS21/Design/Uluru/ContractTests/Executing/#HExecutingContractTestsLocally

#### Pre-Requisites (One-Time Installations)
1. Install [sam-cli](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install-linux.html) and [cfn-cli](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/what-is-cloudformation-cli.html#resource-type-setup).
1. Install [docker](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install-linux.html#serverless-sam-cli-install-linux-docker).
1. Install [mvn](https://w.amazon.com/bin/view/Maven/AmazonLinux/).
1. Install `ada` command-line to refresh aws credentials.

#### How to run
1. SAM Deploy CFN resources from `AWSCloudFormationResourceProvidersSageMakerDeployer` package to your personal-stack account, running the following commands from the Deployer package:
   ```bash
   bb clean && bbr
   sam package && sam deploy
   ```
1. Refresh ada credentials with personal-stack account, where the Deployer package resources are deployed.
1. Run `sam local start-lambda --region us-west-2` from `aws-sagemaker-space` resource directory in AWSCloudFormationResourceProvidersSageMaker workspace.    
   This starts a local lambda service (listener process) on the endpoint `http://127.0.0.1:3001/`, using docker container.
1. Run `cfn generate` followed by `mvn clean package` to build the project, from another terminal window from the same `aws-sagemaker-space` resource directory in AWSCloudFormationResourceProvidersSageMaker workspace.
1. Run `cfn test --region us-west-2`.

**Note-1:** `sam local` and `cfn test` commands need to supplied with the right region where you want to run your contract-tests, basically where you have your contract-test dependency resources deployed from the Deployer package. Default region for these is `us-east-1`.

**Note-2:** If you run `cfn test` without first running `cfn generate` and `mvn package`, you'd see ClassNotFoundException errors for HandlerWrapper. To generate these, you need to build the project correct using mvn.
https://aws.amazon.com/premiumsupport/knowledge-center/cloudformation-java-resource-error/

### Verify Contract Test Results from Registration
https://w.amazon.com/bin/view/AWS21/Design/Uluru/ContractTests/Executing/#HCheckingContractTestResultsfromRegistration.    
In order to see how the contract tests will run during registration, you can use `cfn submit` instead of deploying through the pipeline.

#### Pre-Requisites (One-Time Installations)
1. Install [cfn-cli](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/what-is-cloudformation-cli.html#resource-type-setup).
1. Install [mvn](https://w.amazon.com/bin/view/Maven/AmazonLinux/).
1. Install `ada` command-line to refresh aws credentials.

#### How to run
1. Refresh ada credentials with personal-stack account, basically where you want to run the contract-tests. You'd need Admin permissions on this account, to make changes to the execution-role used to run contract-tests.
1. Run `cfn generate` followed by `mvn clean package` to build the project, from the `aws-sagemaker-space` resource directory in AWSCloudFormationResourceProvidersSageMaker workspace.
1. Run `cfn submit --region us-west-2`.    
   This triggers deployment for CFN Stack `CloudFormationManagedUploadInfrastructure`, on your personal-stack stack in the defined region.
1. There's a `CloudFormationManagedUplo-LogAndMetricsDeliveryRol-{random-id}` role created as part of Infrastructure stack deployed above. This is what is used to execute contract-tests, when run via `cfn submit`.   
   Add an inline policy `ContractTestLogDeliveryRolePolicy` to this role, with the following permissions:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Action": [
           "s3:PutObject"
         ],
         "Resource": [
           "*"
         ],
         "Effect": "Allow"
       },
       {
         "Action": [
           "kms:Encrypt",
           "kms:Decrypt",
           "kms:ReEncrypt*",
           "kms:GenerateDataKey*",
           "kms:DescribeKey"
         ],
         "Resource": "*",
         "Effect": "Allow"
       }
     ]
   }
   ```
1. Update the role's Trust Relationship with:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Principal": {
           "Service": [
             "cloudformation.amazonaws.com",
              "resources.cloudformation.amazonaws.com"
           ]
         },
         "Action": "sts:AssumeRole"
       }
     ]
   }
   ```
1. Add any missing permissions to this role, needed to create your CFN resources.     
   Basically ensure you have everything, that is added to `AWSCloudFormationResourceServiceRole`, which is used to run the contract-tests from the pipeline.
1. Finally, submit a registration of the CFN resources by running the following command:
   ```bash
   # Use the LogAndMetricsDeliveryRole ARN, created in Infrastructure stack above.
   cfn submit --region us-west-2 --role-arn arn:aws:iam::<acount-id>:role/CloudFormationManagedUplo-LogAndMetricsDeliveryRol-{random-id}
   ```
1. ArtifactBucket created in Infrastructure stack holds the results and logs from contract-test run submitted above.      
   You'll find it in the location `cloudformationmanageduploadinfrast-artifactbucket-<random-id>/CloudFormation/ContractTestResults/AWS::SageMaker::Space/<registration-token>.zip`.
