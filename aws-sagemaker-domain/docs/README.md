# AWS::SageMaker::Domain

Resource Type definition for AWS::SageMaker::Domain

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::Domain",
    "Properties" : {
        "<a href="#appnetworkaccesstype" title="AppNetworkAccessType">AppNetworkAccessType</a>" : <i>String</i>,
        "<a href="#authmode" title="AuthMode">AuthMode</a>" : <i>String</i>,
        "<a href="#defaultusersettings" title="DefaultUserSettings">DefaultUserSettings</a>" : <i><a href="usersettings.md">UserSettings</a></i>,
        "<a href="#domainname" title="DomainName">DomainName</a>" : <i>String</i>,
        "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
        "<a href="#subnetids" title="SubnetIds">SubnetIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#vpcid" title="VpcId">VpcId</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::Domain
Properties:
    <a href="#appnetworkaccesstype" title="AppNetworkAccessType">AppNetworkAccessType</a>: <i>String</i>
    <a href="#authmode" title="AuthMode">AuthMode</a>: <i>String</i>
    <a href="#defaultusersettings" title="DefaultUserSettings">DefaultUserSettings</a>: <i><a href="usersettings.md">UserSettings</a></i>
    <a href="#domainname" title="DomainName">DomainName</a>: <i>String</i>
    <a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
    <a href="#subnetids" title="SubnetIds">SubnetIds</a>: <i>
      - String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#vpcid" title="VpcId">VpcId</a>: <i>String</i>
</pre>

## Properties

#### AppNetworkAccessType

Specifies the VPC used for non-EFS traffic. The default value is PublicInternetOnly.

_Required_: No

_Type_: String

_Allowed Values_: <code>PublicInternetOnly</code> | <code>VpcOnly</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AuthMode

The mode of authentication that members use to access the domain.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>SSO</code> | <code>IAM</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### DefaultUserSettings

A collection of settings that apply to users of Amazon SageMaker Studio. These settings are specified when the CreateUserProfile API is called, and as DefaultUserSettings when the CreateDomain API is called.

_Required_: Yes

_Type_: <a href="usersettings.md">UserSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DomainName

A name for the domain.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### KmsKeyId

SageMaker uses AWS KMS to encrypt the EFS volume attached to the domain with an AWS managed customer master key (CMK) by default.

_Required_: No

_Type_: String

_Maximum_: <code>2048</code>

_Pattern_: <code>.*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SubnetIds

The VPC subnets that Studio uses for communication.

_Required_: Yes

_Type_: List of String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

A list of tags to apply to the user profile.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### VpcId

The ID of the Amazon Virtual Private Cloud (VPC) that Studio uses for communication.

_Required_: Yes

_Type_: String

_Maximum_: <code>32</code>

_Pattern_: <code>[-0-9a-zA-Z]+</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the DomainId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### DomainArn

The Amazon Resource Name (ARN) of the created domain.

#### Url

The URL to the created domain.

#### DomainId

The domain name.

#### HomeEfsFileSystemId

The ID of the Amazon Elastic File System (EFS) managed by this Domain.

#### SingleSignOnManagedApplicationInstanceId

The SSO managed application instance ID.

