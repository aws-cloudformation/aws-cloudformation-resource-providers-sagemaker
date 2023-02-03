# AWS::SageMaker::Space KernelGatewayAppSettings

The kernel gateway app settings.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#customimages" title="CustomImages">CustomImages</a>" : <i>[ <a href="customimage.md">CustomImage</a>, ... ]</i>,
    "<a href="#defaultresourcespec" title="DefaultResourceSpec">DefaultResourceSpec</a>" : <i><a href="resourcespec.md">ResourceSpec</a></i>
}
</pre>

### YAML

<pre>
<a href="#customimages" title="CustomImages">CustomImages</a>: <i>
      - <a href="customimage.md">CustomImage</a></i>
<a href="#defaultresourcespec" title="DefaultResourceSpec">DefaultResourceSpec</a>: <i><a href="resourcespec.md">ResourceSpec</a></i>
</pre>

## Properties

#### CustomImages

A list of custom SageMaker images that are configured to run as a KernelGateway app.

_Required_: No

_Type_: List of <a href="customimage.md">CustomImage</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DefaultResourceSpec

_Required_: No

_Type_: <a href="resourcespec.md">ResourceSpec</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

