# AWS::SageMaker::ModelPackage UserContext

Information about the user who created or modified an experiment, trial, trial component, lineage group, or project.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#domainid" title="DomainId">DomainId</a>" : <i>String</i>,
    "<a href="#userprofilearn" title="UserProfileArn">UserProfileArn</a>" : <i>String</i>,
    "<a href="#userprofilename" title="UserProfileName">UserProfileName</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#domainid" title="DomainId">DomainId</a>: <i>String</i>
<a href="#userprofilearn" title="UserProfileArn">UserProfileArn</a>: <i>String</i>
<a href="#userprofilename" title="UserProfileName">UserProfileName</a>: <i>String</i>
</pre>

## Properties

#### DomainId

The domain associated with the user.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UserProfileArn

The Amazon Resource Name (ARN) of the user's profile.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UserProfileName

The name of the user's profile.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

