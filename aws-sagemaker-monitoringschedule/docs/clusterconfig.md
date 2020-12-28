# AWS::SageMaker::MonitoringSchedule ClusterConfig

Configuration for the cluster used to run model monitoring jobs.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#instancecount" title="InstanceCount">InstanceCount</a>" : <i>Integer</i>,
    "<a href="#instancetype" title="InstanceType">InstanceType</a>" : <i>String</i>,
    "<a href="#volumekmskeyid" title="VolumeKmsKeyId">VolumeKmsKeyId</a>" : <i>String</i>,
    "<a href="#volumesizeingb" title="VolumeSizeInGB">VolumeSizeInGB</a>" : <i>Integer</i>
}
</pre>

### YAML

<pre>
<a href="#instancecount" title="InstanceCount">InstanceCount</a>: <i>Integer</i>
<a href="#instancetype" title="InstanceType">InstanceType</a>: <i>String</i>
<a href="#volumekmskeyid" title="VolumeKmsKeyId">VolumeKmsKeyId</a>: <i>String</i>
<a href="#volumesizeingb" title="VolumeSizeInGB">VolumeSizeInGB</a>: <i>Integer</i>
</pre>

## Properties

#### InstanceCount

The number of ML compute instances to use in the model monitoring job. For distributed processing jobs, specify a value greater than 1. The default value is 1.

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### InstanceType

The ML compute instance type for the processing job.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VolumeKmsKeyId

The AWS Key Management Service (AWS KMS) key that Amazon SageMaker uses to encrypt data on the storage volume attached to the ML compute instance(s) that run the model monitoring job.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VolumeSizeInGB

The size of the ML storage volume, in gigabytes, that you want to provision. You must specify sufficient ML storage for your scenario.

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

