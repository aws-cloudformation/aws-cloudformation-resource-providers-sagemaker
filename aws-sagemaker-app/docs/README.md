# AWS::SageMaker::App

Resource Type definition for AWS::SageMaker::App

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::App",
    "Properties" : {
        "<a href="#appname" title="AppName">AppName</a>" : <i>String</i>,
        "<a href="#apptype" title="AppType">AppType</a>" : <i>String</i>,
        "<a href="#domainid" title="DomainId">DomainId</a>" : <i>String</i>,
        "<a href="#resourcespec" title="ResourceSpec">ResourceSpec</a>" : <i><a href="resourcespec.md">ResourceSpec</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#userprofilename" title="UserProfileName">UserProfileName</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::App
Properties:
    <a href="#appname" title="AppName">AppName</a>: <i>String</i>
    <a href="#apptype" title="AppType">AppType</a>: <i>String</i>
    <a href="#domainid" title="DomainId">DomainId</a>: <i>String</i>
    <a href="#resourcespec" title="ResourceSpec">ResourceSpec</a>: <i><a href="resourcespec.md">ResourceSpec</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#userprofilename" title="UserProfileName">UserProfileName</a>: <i>String</i>
</pre>

## Properties

#### AppName

The name of the app.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AppType

The type of app.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>JupyterServer</code> | <code>KernelGateway</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### DomainId

The domain ID.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ResourceSpec

_Required_: No

_Type_: <a href="resourcespec.md">ResourceSpec</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

A list of tags to apply to the app.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### UserProfileName

The user profile name.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AppArn

The Amazon Resource Name (ARN) of the app.

