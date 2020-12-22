# AWS::SageMaker::MonitoringSchedule NetworkConfig

Networking options for a job, such as network traffic encryption between containers, whether to allow inbound and outbound network calls to and from containers, and the VPC subnets and security groups to use for VPC-enabled jobs.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#enableintercontainertrafficencryption" title="EnableInterContainerTrafficEncryption">EnableInterContainerTrafficEncryption</a>" : <i>Boolean</i>,
    "<a href="#enablenetworkisolation" title="EnableNetworkIsolation">EnableNetworkIsolation</a>" : <i>Boolean</i>,
    "<a href="#vpcconfig" title="VpcConfig">VpcConfig</a>" : <i><a href="vpcconfig.md">VpcConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#enableintercontainertrafficencryption" title="EnableInterContainerTrafficEncryption">EnableInterContainerTrafficEncryption</a>: <i>Boolean</i>
<a href="#enablenetworkisolation" title="EnableNetworkIsolation">EnableNetworkIsolation</a>: <i>Boolean</i>
<a href="#vpcconfig" title="VpcConfig">VpcConfig</a>: <i><a href="vpcconfig.md">VpcConfig</a></i>
</pre>

## Properties

#### EnableInterContainerTrafficEncryption

Whether to encrypt all communications between distributed processing jobs. Choose True to encrypt communications. Encryption provides greater security for distributed processing jobs, but the processing might take longer.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EnableNetworkIsolation

Whether to allow inbound and outbound network calls to and from the containers used for the processing job.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcConfig

Specifies a VPC that your training jobs and hosted models have access to. Control access to and from your training and model containers by configuring the VPC.

_Required_: No

_Type_: <a href="vpcconfig.md">VpcConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

