# AWS::SageMaker::Pipeline

Resource Type definition for AWS::SageMaker::Pipeline

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::Pipeline",
    "Properties" : {
        "<a href="#pipelinename" title="PipelineName">PipelineName</a>" : <i>String</i>,
        "<a href="#pipelinedisplayname" title="PipelineDisplayName">PipelineDisplayName</a>" : <i>String</i>,
        "<a href="#pipelinedescription" title="PipelineDescription">PipelineDescription</a>" : <i>String</i>,
        "<a href="#pipelinedefinition" title="PipelineDefinition">PipelineDefinition</a>" : <i><a href="pipelinedefinition.md">PipelineDefinition</a></i>,
        "<a href="#rolearn" title="RoleArn">RoleArn</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::Pipeline
Properties:
    <a href="#pipelinename" title="PipelineName">PipelineName</a>: <i>String</i>
    <a href="#pipelinedisplayname" title="PipelineDisplayName">PipelineDisplayName</a>: <i>String</i>
    <a href="#pipelinedescription" title="PipelineDescription">PipelineDescription</a>: <i>String</i>
    <a href="#pipelinedefinition" title="PipelineDefinition">PipelineDefinition</a>: <i><a href="pipelinedefinition.md">PipelineDefinition</a></i>
    <a href="#rolearn" title="RoleArn">RoleArn</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### PipelineName

The name of the Pipeline.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### PipelineDisplayName

The display name of the Pipeline.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PipelineDescription

The description of the Pipeline.

_Required_: No

_Type_: String

_Maximum_: <code>3072</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PipelineDefinition

_Required_: No

_Type_: <a href="pipelinedefinition.md">PipelineDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RoleArn

Role Arn

_Required_: Yes

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the PipelineName.
