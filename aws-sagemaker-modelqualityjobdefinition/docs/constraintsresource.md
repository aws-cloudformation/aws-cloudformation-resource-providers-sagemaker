# AWS::SageMaker::ModelQualityJobDefinition ConstraintsResource

The baseline constraints resource for a monitoring job.

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

The Amazon S3 URI.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

