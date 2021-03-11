# AWS::SageMaker::Domain CustomImage

A custom SageMaker image.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#appimageconfigname" title="AppImageConfigName">AppImageConfigName</a>" : <i>String</i>,
    "<a href="#imagename" title="ImageName">ImageName</a>" : <i>String</i>,
    "<a href="#imageversionnumber" title="ImageVersionNumber">ImageVersionNumber</a>" : <i>Integer</i>
}
</pre>

### YAML

<pre>
<a href="#appimageconfigname" title="AppImageConfigName">AppImageConfigName</a>: <i>String</i>
<a href="#imagename" title="ImageName">ImageName</a>: <i>String</i>
<a href="#imageversionnumber" title="ImageVersionNumber">ImageVersionNumber</a>: <i>Integer</i>
</pre>

## Properties

#### AppImageConfigName

The Name of the AppImageConfig.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageName

The name of the CustomImage. Must be unique to your account.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9]([-.]?[a-zA-Z0-9]){0,62}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageVersionNumber

The version number of the CustomImage.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

