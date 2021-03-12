# AWS::SageMaker::AppImageConfig

Resource Type definition for AWS::SageMaker::AppImageConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::AppImageConfig",
    "Properties" : {
        "<a href="#appimageconfigname" title="AppImageConfigName">AppImageConfigName</a>" : <i>String</i>,
        "<a href="#kernelgatewayimageconfig" title="KernelGatewayImageConfig">KernelGatewayImageConfig</a>" : <i><a href="kernelgatewayimageconfig.md">KernelGatewayImageConfig</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::AppImageConfig
Properties:
    <a href="#appimageconfigname" title="AppImageConfigName">AppImageConfigName</a>: <i>String</i>
    <a href="#kernelgatewayimageconfig" title="KernelGatewayImageConfig">KernelGatewayImageConfig</a>: <i><a href="kernelgatewayimageconfig.md">KernelGatewayImageConfig</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### AppImageConfigName

The Name of the AppImageConfig.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### KernelGatewayImageConfig

The configuration for the file system and kernels in a SageMaker image running as a KernelGateway app.

_Required_: No

_Type_: <a href="kernelgatewayimageconfig.md">KernelGatewayImageConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

A list of tags to apply to the AppImageConfig.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the AppImageConfigName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AppImageConfigArn

The Amazon Resource Name (ARN) of the AppImageConfig.

