# AWS::SageMaker::ModelExplainabilityJobDefinition ModelExplainabilityBaselineConfig

Baseline configuration used to validate that the data conforms to the specified constraints and statistics.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#baseliningjobname" title="BaseliningJobName">BaseliningJobName</a>" : <i>String</i>,
    "<a href="#constraintsresource" title="ConstraintsResource">ConstraintsResource</a>" : <i><a href="constraintsresource.md">ConstraintsResource</a></i>
}
</pre>

### YAML

<pre>
<a href="#baseliningjobname" title="BaseliningJobName">BaseliningJobName</a>: <i>String</i>
<a href="#constraintsresource" title="ConstraintsResource">ConstraintsResource</a>: <i><a href="constraintsresource.md">ConstraintsResource</a></i>
</pre>

## Properties

#### BaseliningJobName

The name of a processing job

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ConstraintsResource

The baseline constraints resource for a monitoring job.

_Required_: No

_Type_: <a href="constraintsresource.md">ConstraintsResource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

