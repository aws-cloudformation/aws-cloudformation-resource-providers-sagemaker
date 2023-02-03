# AWS::SageMaker::Space

Resource Type definition for AWS::SageMaker::Space

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::Space",
    "Properties" : {
        "<a href="#domainid" title="DomainId">DomainId</a>" : <i>String</i>,
        "<a href="#spacename" title="SpaceName">SpaceName</a>" : <i>String</i>,
        "<a href="#spacesettings" title="SpaceSettings">SpaceSettings</a>" : <i><a href="spacesettings.md">SpaceSettings</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::Space
Properties:
    <a href="#domainid" title="DomainId">DomainId</a>: <i>String</i>
    <a href="#spacename" title="SpaceName">SpaceName</a>: <i>String</i>
    <a href="#spacesettings" title="SpaceSettings">SpaceSettings</a>: <i><a href="spacesettings.md">SpaceSettings</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### DomainId

The ID of the associated Domain.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SpaceName

A name for the Space.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SpaceSettings

A collection of settings that apply to spaces of Amazon SageMaker Studio. These settings are specified when the CreateSpace API is called.

_Required_: No

_Type_: <a href="spacesettings.md">SpaceSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

A list of tags to apply to the space.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### SpaceArn

The space Amazon Resource Name (ARN).

