# AWS::SageMaker::ModelPackage ModelPackageStatusItem

Represents the overall status of a model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#failurereason" title="FailureReason">FailureReason</a>" : <i>String</i>,
    "<a href="#name" title="Name">Name</a>" : <i>String</i>,
    "<a href="#status" title="Status">Status</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#failurereason" title="FailureReason">FailureReason</a>: <i>String</i>
<a href="#name" title="Name">Name</a>: <i>String</i>
<a href="#status" title="Status">Status</a>: <i>String</i>
</pre>

## Properties

#### FailureReason

If the overall status is Failed, the reason for the failure.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name of the model package for which the overall status is being reported.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Status

The current status.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>NotStarted</code> | <code>Failed</code> | <code>InProgress</code> | <code>Completed</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

