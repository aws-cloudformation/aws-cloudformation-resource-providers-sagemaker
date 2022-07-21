# AWS::SageMaker::ModelPackage TransformOutput

Describes the results of a transform job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#accept" title="Accept">Accept</a>" : <i>String</i>,
    "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
    "<a href="#s3outputpath" title="S3OutputPath">S3OutputPath</a>" : <i>String</i>,
    "<a href="#assemblewith" title="AssembleWith">AssembleWith</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#accept" title="Accept">Accept</a>: <i>String</i>
<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
<a href="#s3outputpath" title="S3OutputPath">S3OutputPath</a>: <i>String</i>
<a href="#assemblewith" title="AssembleWith">AssembleWith</a>: <i>String</i>
</pre>

## Properties

#### Accept

The MIME type used to specify the output data. Amazon SageMaker uses the MIME type with each http call to transfer data from the transform job.

_Required_: No

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KmsKeyId

The AWS Key Management Service (AWS KMS) key that Amazon SageMaker uses to encrypt the model artifacts at rest using Amazon S3 server-side encryption.

_Required_: No

_Type_: String

_Maximum_: <code>2048</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3OutputPath

The Amazon S3 path where you want Amazon SageMaker to store the results of the transform job.

_Required_: Yes

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AssembleWith

Defines how to assemble the results of the transform job as a single S3 object.

_Required_: No

_Type_: String

_Allowed Values_: <code>None</code> | <code>Line</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

