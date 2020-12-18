# AWS::SageMaker::Pipeline PipelineDefinition

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#pipelinedefinitionbody" title="PipelineDefinitionBody">PipelineDefinitionBody</a>" : <i>String</i>,
    "<a href="#pipelinedefinitions3location" title="PipelineDefinitionS3Location">PipelineDefinitionS3Location</a>" : <i><a href="s3location.md">S3Location</a></i>
}
</pre>

### YAML

<pre>
<a href="#pipelinedefinitionbody" title="PipelineDefinitionBody">PipelineDefinitionBody</a>: <i>String</i>
<a href="#pipelinedefinitions3location" title="PipelineDefinitionS3Location">PipelineDefinitionS3Location</a>: <i><a href="s3location.md">S3Location</a></i>
</pre>

## Properties

#### PipelineDefinitionBody

A specification that defines the pipeline in JSON format.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PipelineDefinitionS3Location

_Required_: Yes

_Type_: <a href="s3location.md">S3Location</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

