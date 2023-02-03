# AWS::SageMaker::Domain RStudioServerProDomainSettings

A collection of settings that update the current configuration for the RStudioServerPro Domain-level app.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#domainexecutionrolearn" title="DomainExecutionRoleArn">DomainExecutionRoleArn</a>" : <i>String</i>,
    "<a href="#rstudioconnecturl" title="RStudioConnectUrl">RStudioConnectUrl</a>" : <i>String</i>,
    "<a href="#rstudiopackagemanagerurl" title="RStudioPackageManagerUrl">RStudioPackageManagerUrl</a>" : <i>String</i>,
    "<a href="#defaultresourcespec" title="DefaultResourceSpec">DefaultResourceSpec</a>" : <i><a href="resourcespec.md">ResourceSpec</a></i>
}
</pre>

### YAML

<pre>
<a href="#domainexecutionrolearn" title="DomainExecutionRoleArn">DomainExecutionRoleArn</a>: <i>String</i>
<a href="#rstudioconnecturl" title="RStudioConnectUrl">RStudioConnectUrl</a>: <i>String</i>
<a href="#rstudiopackagemanagerurl" title="RStudioPackageManagerUrl">RStudioPackageManagerUrl</a>: <i>String</i>
<a href="#defaultresourcespec" title="DefaultResourceSpec">DefaultResourceSpec</a>: <i><a href="resourcespec.md">ResourceSpec</a></i>
</pre>

## Properties

#### DomainExecutionRoleArn

The ARN of the execution role for the RStudioServerPro Domain-level app.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RStudioConnectUrl

A URL pointing to an RStudio Connect server.

_Required_: No

_Type_: String

_Pattern_: <code>^(https:|http:|www\.)\S*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RStudioPackageManagerUrl

A URL pointing to an RStudio Package Manager server.

_Required_: No

_Type_: String

_Pattern_: <code>^(https:|http:|www\.)\S*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DefaultResourceSpec

_Required_: No

_Type_: <a href="resourcespec.md">ResourceSpec</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

