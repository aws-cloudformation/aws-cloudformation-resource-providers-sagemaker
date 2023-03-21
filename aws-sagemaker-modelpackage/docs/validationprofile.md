# AWS::SageMaker::ModelPackage ValidationProfile

Contains data, such as the inputs and targeted instance types that are used in the process of validating the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#transformjobdefinition" title="TransformJobDefinition">TransformJobDefinition</a>" : <i><a href="transformjobdefinition.md">TransformJobDefinition</a></i>,
    "<a href="#profilename" title="ProfileName">ProfileName</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#transformjobdefinition" title="TransformJobDefinition">TransformJobDefinition</a>: <i><a href="transformjobdefinition.md">TransformJobDefinition</a></i>
<a href="#profilename" title="ProfileName">ProfileName</a>: <i>String</i>
</pre>

## Properties

#### TransformJobDefinition

Defines the input needed to run a transform job using the inference specification specified in the algorithm.

_Required_: Yes

_Type_: <a href="transformjobdefinition.md">TransformJobDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProfileName

The name of the profile for the model package.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

