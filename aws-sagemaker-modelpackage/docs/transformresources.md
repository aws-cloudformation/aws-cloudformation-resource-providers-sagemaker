# AWS::SageMaker::ModelPackage TransformResources

Describes the resources, including ML instance types and ML instance count, to use for transform job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#instancecount" title="InstanceCount">InstanceCount</a>" : <i>Integer</i>,
    "<a href="#instancetype" title="InstanceType">InstanceType</a>" : <i>String</i>,
    "<a href="#volumekmskeyid" title="VolumeKmsKeyId">VolumeKmsKeyId</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#instancecount" title="InstanceCount">InstanceCount</a>: <i>Integer</i>
<a href="#instancetype" title="InstanceType">InstanceType</a>: <i>String</i>
<a href="#volumekmskeyid" title="VolumeKmsKeyId">VolumeKmsKeyId</a>: <i>String</i>
</pre>

## Properties

#### InstanceCount

The number of ML compute instances to use in the transform job. For distributed transform jobs, specify a value greater than 1. The default value is 1.

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### InstanceType

The ML compute instance type for the transform job.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VolumeKmsKeyId

The AWS Key Management Service (AWS KMS) key that Amazon SageMaker uses to encrypt model data on the storage volume attached to the ML compute instance(s) that run the batch transform job.

_Required_: No

_Type_: String

_Maximum_: <code>2048</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

