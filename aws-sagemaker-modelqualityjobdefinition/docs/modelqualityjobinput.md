# AWS::SageMaker::ModelQualityJobDefinition ModelQualityJobInput

The inputs for a monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#endpointinput" title="EndpointInput">EndpointInput</a>" : <i><a href="endpointinput.md">EndpointInput</a></i>,
    "<a href="#groundtruths3input" title="GroundTruthS3Input">GroundTruthS3Input</a>" : <i><a href="monitoringgroundtruths3input.md">MonitoringGroundTruthS3Input</a></i>
}
</pre>

### YAML

<pre>
<a href="#endpointinput" title="EndpointInput">EndpointInput</a>: <i><a href="endpointinput.md">EndpointInput</a></i>
<a href="#groundtruths3input" title="GroundTruthS3Input">GroundTruthS3Input</a>: <i><a href="monitoringgroundtruths3input.md">MonitoringGroundTruthS3Input</a></i>
</pre>

## Properties

#### EndpointInput

The endpoint for a monitoring job.

_Required_: Yes

_Type_: <a href="endpointinput.md">EndpointInput</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### GroundTruthS3Input

Ground truth input provided in S3 

_Required_: Yes

_Type_: <a href="monitoringgroundtruths3input.md">MonitoringGroundTruthS3Input</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

