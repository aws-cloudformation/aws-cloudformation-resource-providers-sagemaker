# AWS::SageMaker::Domain UserSettings

A collection of settings that apply to users of Amazon SageMaker Studio. These settings are specified when the CreateUserProfile API is called, and as DefaultUserSettings when the CreateDomain API is called.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#executionrole" title="ExecutionRole">ExecutionRole</a>" : <i>String</i>,
    "<a href="#jupyterserverappsettings" title="JupyterServerAppSettings">JupyterServerAppSettings</a>" : <i><a href="jupyterserverappsettings.md">JupyterServerAppSettings</a></i>,
    "<a href="#kernelgatewayappsettings" title="KernelGatewayAppSettings">KernelGatewayAppSettings</a>" : <i><a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a></i>,
    "<a href="#rstudioserverproappsettings" title="RStudioServerProAppSettings">RStudioServerProAppSettings</a>" : <i><a href="rstudioserverproappsettings.md">RStudioServerProAppSettings</a></i>,
    "<a href="#rsessionappsettings" title="RSessionAppSettings">RSessionAppSettings</a>" : <i><a href="rsessionappsettings.md">RSessionAppSettings</a></i>,
    "<a href="#securitygroups" title="SecurityGroups">SecurityGroups</a>" : <i>[ String, ... ]</i>,
    "<a href="#sharingsettings" title="SharingSettings">SharingSettings</a>" : <i><a href="sharingsettings.md">SharingSettings</a></i>
}
</pre>

### YAML

<pre>
<a href="#executionrole" title="ExecutionRole">ExecutionRole</a>: <i>String</i>
<a href="#jupyterserverappsettings" title="JupyterServerAppSettings">JupyterServerAppSettings</a>: <i><a href="jupyterserverappsettings.md">JupyterServerAppSettings</a></i>
<a href="#kernelgatewayappsettings" title="KernelGatewayAppSettings">KernelGatewayAppSettings</a>: <i><a href="kernelgatewayappsettings.md">KernelGatewayAppSettings</a></i>
<a href="#rstudioserverproappsettings" title="RStudioServerProAppSettings">RStudioServerProAppSettings</a>: <i><a href="rstudioserverproappsettings.md">RStudioServerProAppSettings</a></i>
<a href="#rsessionappsettings" title="RSessionAppSettings">RSessionAppSettings</a>: <i><a href="rsessionappsettings.md">RSessionAppSettings</a></i>
<a href="#securitygroups" title="SecurityGroups">SecurityGroups</a>: <i>
      - String</i>
<a href="#sharingsettings" title="SharingSettings">SharingSettings</a>: <i><a href="sharingsettings.md">SharingSettings</a></i>
</pre>

## Properties

#### ExecutionRole

The execution role for the user.

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

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

#### RStudioServerProAppSettings

A collection of settings that configure user interaction with the RStudioServerPro app.

_Required_: No

_Type_: <a href="rstudioserverproappsettings.md">RStudioServerProAppSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RSessionAppSettings

A collection of settings that apply to an RSessionGateway app.

_Required_: No

_Type_: <a href="rsessionappsettings.md">RSessionAppSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SecurityGroups

The security groups for the Amazon Virtual Private Cloud (VPC) that Studio uses for communication.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SharingSettings

Specifies options when sharing an Amazon SageMaker Studio notebook. These settings are specified as part of DefaultUserSettings when the CreateDomain API is called, and as part of UserSettings when the CreateUserProfile API is called.

_Required_: No

_Type_: <a href="sharingsettings.md">SharingSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

