# AWS::SageMaker::Space SpaceSettings

A collection of settings that apply to spaces of Amazon SageMaker Studio. These settings are specified when the CreateSpace API is called.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#jupyterserverappsettings" title="JupyterServerAppSettings">JupyterServerAppSettings</a>" : <i><a href="jupyterserverappsettings.md">JupyterServerAppSettings</a></i>,
    "<a href="#kernelgatewayappsettings" title="KernelGatewayAppSettings">KernelGatewayAppSettings</a>" : <i><a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a></i>
}
</pre>

### YAML

<pre>
<a href="#jupyterserverappsettings" title="JupyterServerAppSettings">JupyterServerAppSettings</a>: <i><a href="jupyterserverappsettings.md">JupyterServerAppSettings</a></i>
<a href="#kernelgatewayappsettings" title="KernelGatewayAppSettings">KernelGatewayAppSettings</a>: <i><a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a></i>
</pre>

## Properties

#### JupyterServerAppSettings

The JupyterServer app settings.

_Required_: No

_Type_: <a href="jupyterserverappsettings.md">JupyterServerAppSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KernelGatewayAppSettings

The kernel gateway app settings.

_Required_: No

_Type_: <a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

