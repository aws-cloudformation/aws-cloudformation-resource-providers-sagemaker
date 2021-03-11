# AWS::SageMaker::App ResourceSpec

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#instancetype" title="InstanceType">InstanceType</a>" : <i>String</i>,
    "<a href="#sagemakerimagearn" title="SageMakerImageArn">SageMakerImageArn</a>" : <i>String</i>,
    "<a href="#sagemakerimageversionarn" title="SageMakerImageVersionArn">SageMakerImageVersionArn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#instancetype" title="InstanceType">InstanceType</a>: <i>String</i>
<a href="#sagemakerimagearn" title="SageMakerImageArn">SageMakerImageArn</a>: <i>String</i>
<a href="#sagemakerimageversionarn" title="SageMakerImageVersionArn">SageMakerImageVersionArn</a>: <i>String</i>
</pre>

## Properties

#### InstanceType

The instance type that the image version runs on.

_Required_: No

_Type_: String

_Allowed Values_: <code>system</code> | <code>ml.t3.micro</code> | <code>ml.t3.small</code> | <code>ml.t3.medium</code> | <code>ml.t3.large</code> | <code>ml.t3.xlarge</code> | <code>ml.t3.2xlarge</code> | <code>ml.m5.large</code> | <code>ml.m5.xlarge</code> | <code>ml.m5.2xlarge</code> | <code>ml.m5.4xlarge</code> | <code>ml.m5.8xlarge</code> | <code>ml.m5.12xlarge</code> | <code>ml.m5.16xlarge</code> | <code>ml.m5.24xlarge</code> | <code>ml.c5.large</code> | <code>ml.c5.xlarge</code> | <code>ml.c5.2xlarge</code> | <code>ml.c5.4xlarge</code> | <code>ml.c5.9xlarge</code> | <code>ml.c5.12xlarge</code> | <code>ml.c5.18xlarge</code> | <code>ml.c5.24xlarge</code> | <code>ml.p3.2xlarge</code> | <code>ml.p3.8xlarge</code> | <code>ml.p3.16xlarge</code> | <code>ml.g4dn.xlarge</code> | <code>ml.g4dn.2xlarge</code> | <code>ml.g4dn.4xlarge</code> | <code>ml.g4dn.8xlarge</code> | <code>ml.g4dn.12xlarge</code> | <code>ml.g4dn.16xlarge</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SageMakerImageArn

The ARN of the SageMaker image that the image version belongs to.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^arn:aws(-[\w]+)*:sagemaker:.+:[0-9]{12}:image/[a-z0-9]([-.]?[a-z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SageMakerImageVersionArn

The ARN of the image version created on the instance.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^arn:aws(-[\w]+)*:sagemaker:.+:[0-9]{12}:image-version/[a-z0-9]([-.]?[a-z0-9])*/[0-9]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

