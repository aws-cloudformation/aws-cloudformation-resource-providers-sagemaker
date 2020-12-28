# AWS::SageMaker::ModelQualityJobDefinition MonitoringOutputConfig

The output configuration for monitoring jobs.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
    "<a href="#monitoringoutputs" title="MonitoringOutputs">MonitoringOutputs</a>" : <i>[ <a href="monitoringoutput.md">MonitoringOutput</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
<a href="#monitoringoutputs" title="MonitoringOutputs">MonitoringOutputs</a>: <i>
      - <a href="monitoringoutput.md">MonitoringOutput</a></i>
</pre>

## Properties

#### KmsKeyId

The AWS Key Management Service (AWS KMS) key that Amazon SageMaker uses to encrypt the model artifacts at rest using Amazon S3 server-side encryption.

_Required_: No

_Type_: String

_Maximum_: <code>2048</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringOutputs

Monitoring outputs for monitoring jobs. This is where the output of the periodic monitoring jobs is uploaded.

_Required_: Yes

_Type_: List of <a href="monitoringoutput.md">MonitoringOutput</a>

_Minimum_: <code>1</code>

_Maximum_: <code>1</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

