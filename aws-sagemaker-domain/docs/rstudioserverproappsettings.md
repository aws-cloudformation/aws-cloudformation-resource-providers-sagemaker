# AWS::SageMaker::Domain RStudioServerProAppSettings

A collection of settings that configure user interaction with the RStudioServerPro app.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#accessstatus" title="AccessStatus">AccessStatus</a>" : <i>String</i>,
    "<a href="#usergroup" title="UserGroup">UserGroup</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#accessstatus" title="AccessStatus">AccessStatus</a>: <i>String</i>
<a href="#usergroup" title="UserGroup">UserGroup</a>: <i>String</i>
</pre>

## Properties

#### AccessStatus

Indicates whether the current user has access to the RStudioServerPro app.

_Required_: No

_Type_: String

_Allowed Values_: <code>ENABLED</code> | <code>DISABLED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UserGroup

The level of permissions that the user has within the RStudioServerPro app. This value defaults to User. The Admin value allows the user access to the RStudio Administrative Dashboard.

_Required_: No

_Type_: String

_Allowed Values_: <code>R_STUDIO_ADMIN</code> | <code>R_STUDIO_USER</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

