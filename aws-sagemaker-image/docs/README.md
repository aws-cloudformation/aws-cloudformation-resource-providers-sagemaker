# AWS::SageMaker::Image

Resource Type definition for AWS::SageMaker::Image

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::Image",
    "Properties" : {
        "<a href="#imagename" title="ImageName">ImageName</a>" : <i>String</i>,
        "<a href="#imagerolearn" title="ImageRoleArn">ImageRoleArn</a>" : <i>String</i>,
        "<a href="#imagedisplayname" title="ImageDisplayName">ImageDisplayName</a>" : <i>String</i>,
        "<a href="#imagedescription" title="ImageDescription">ImageDescription</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::Image
Properties:
    <a href="#imagename" title="ImageName">ImageName</a>: <i>String</i>
    <a href="#imagerolearn" title="ImageRoleArn">ImageRoleArn</a>: <i>String</i>
    <a href="#imagedisplayname" title="ImageDisplayName">ImageDisplayName</a>: <i>String</i>
    <a href="#imagedescription" title="ImageDescription">ImageDescription</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### ImageName

The name of the image.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9]([-.]?[a-zA-Z0-9])*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ImageRoleArn

The Amazon Resource Name (ARN) of an IAM role that enables Amazon SageMaker to perform tasks on behalf of the customer.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^arn:aws(-[\w]+)*:iam::[0-9]{12}:role/.*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageDisplayName

The display name of the image.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>128</code>

_Pattern_: <code>^[A-Za-z0-9 -_]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageDescription

A description of the image.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>512</code>

_Pattern_: <code>.+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ImageArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ImageArn

The Amazon Resource Name (ARN) of the image.

