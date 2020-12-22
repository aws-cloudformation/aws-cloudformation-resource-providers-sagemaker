# AWS::SageMaker::ModelBiasJobDefinition MonitoringGroundTruthS3Input

Ground truth input provided in S3 

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#s3uri" title="S3Uri">S3Uri</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#s3uri" title="S3Uri">S3Uri</a>: <i>String</i>
</pre>

## Properties

#### S3Uri

A URI that identifies the Amazon S3 storage location where Amazon SageMaker saves the results of a monitoring job.

_Required_: Yes

_Type_: String

_Maximum_: <code>512</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

