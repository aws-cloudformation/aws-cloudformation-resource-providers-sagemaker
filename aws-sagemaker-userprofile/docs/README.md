# AWS::SageMaker::UserProfile

Resource Type definition for AWS::SageMaker::UserProfile

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::UserProfile",
    "Properties" : {
        "<a href="#domainid" title="DomainId">DomainId</a>" : <i>String</i>,
        "<a href="#singlesignonuseridentifier" title="SingleSignOnUserIdentifier">SingleSignOnUserIdentifier</a>" : <i>String</i>,
        "<a href="#singlesignonuservalue" title="SingleSignOnUserValue">SingleSignOnUserValue</a>" : <i>String</i>,
        "<a href="#userprofilename" title="UserProfileName">UserProfileName</a>" : <i>String</i>,
        "<a href="#usersettings" title="UserSettings">UserSettings</a>" : <i><a href="usersettings.md">UserSettings</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::UserProfile
Properties:
    <a href="#domainid" title="DomainId">DomainId</a>: <i>String</i>
    <a href="#singlesignonuseridentifier" title="SingleSignOnUserIdentifier">SingleSignOnUserIdentifier</a>: <i>String</i>
    <a href="#singlesignonuservalue" title="SingleSignOnUserValue">SingleSignOnUserValue</a>: <i>String</i>
    <a href="#userprofilename" title="UserProfileName">UserProfileName</a>: <i>String</i>
    <a href="#usersettings" title="UserSettings">UserSettings</a>: <i><a href="usersettings.md">UserSettings</a></i>
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

#### SingleSignOnUserIdentifier

A specifier for the type of value specified in SingleSignOnUserValue. Currently, the only supported value is "UserName". If the Domain's AuthMode is SSO, this field is required. If the Domain's AuthMode is not SSO, this field cannot be specified.

_Required_: No

_Type_: String

_Pattern_: <code>UserName</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SingleSignOnUserValue

The username of the associated AWS Single Sign-On User for this UserProfile. If the Domain's AuthMode is SSO, this field is required, and must match a valid username of a user in your directory. If the Domain's AuthMode is not SSO, this field cannot be specified.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### UserProfileName

A name for the UserProfile.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### UserSettings

A collection of settings that apply to users of Amazon SageMaker Studio. These settings are specified when the CreateUserProfile API is called, and as DefaultUserSettings when the CreateDomain API is called.

_Required_: No

_Type_: <a href="usersettings.md">UserSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

A list of tags to apply to the user profile.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### UserProfileArn

The user profile Amazon Resource Name (ARN).

