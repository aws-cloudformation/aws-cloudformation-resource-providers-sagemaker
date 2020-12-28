# AWS::SageMaker::DataQualityJobDefinition MonitoringResources

Identifies the resources to deploy for a monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#clusterconfig" title="ClusterConfig">ClusterConfig</a>" : <i><a href="clusterconfig.md">ClusterConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#clusterconfig" title="ClusterConfig">ClusterConfig</a>: <i><a href="clusterconfig.md">ClusterConfig</a></i>
</pre>

## Properties

#### ClusterConfig

Configuration for the cluster used to run model monitoring jobs.

_Required_: Yes

_Type_: <a href="clusterconfig.md">ClusterConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

