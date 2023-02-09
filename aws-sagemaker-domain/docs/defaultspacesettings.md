# AWS::SageMaker::Domain DefaultSpaceSettings

A collection of settings that apply to spaces of Amazon SageMaker Studio. These settings are specified when the Create/Update Domain API is called.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#executionrole" title="ExecutionRole">ExecutionRole</a>" : <i>String</i>,
    "<a href="#jupyterserverappsettings" title="JupyterServerAppSettings">JupyterServerAppSettings</a>" : <i><a href="jupyterserverappsettings.md">JupyterServerAppSettings</a></i>,
    "<a href="#kernelgatewayappsettings" title="KernelGatewayAppSettings">KernelGatewayAppSettings</a>" : <i><a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a></i>,
    "<a href="#securitygroups" title="SecurityGroups">SecurityGroups</a>" : <i>[ String, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#executionrole" title="ExecutionRole">ExecutionRole</a>: <i>String</i>
<a href="#jupyterserverappsettings" title="JupyterServerAppSettings">JupyterServerAppSettings</a>: <i><a href="jupyterserverappsettings.md">JupyterServerAppSettings</a></i>
<a href="#kernelgatewayappsettings" title="KernelGatewayAppSettings">KernelGatewayAppSettings</a>: <i><a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a></i>
<a href="#securitygroups" title="SecurityGroups">SecurityGroups</a>: <i>
      - String</i>
</pre>

## Properties

#### ExecutionRole

The execution role for the space.

_Required_: No

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### JupyterServerAppSettings

_Required_: No

_Type_: <a href="jupyterserverappsettings.md">JupyterServerAppSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KernelGatewayAppSettings

_Required_: No

_Type_: <a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SecurityGroups

The security groups for the Amazon Virtual Private Cloud (VPC) that Studio uses for communication.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

