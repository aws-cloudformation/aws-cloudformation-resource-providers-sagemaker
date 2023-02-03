# AWS::SageMaker::Domain DomainSettings

A collection of Domain settings.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#securitygroupids" title="SecurityGroupIds">SecurityGroupIds</a>" : <i>[ String, ... ]</i>,
    "<a href="#rstudioserverprodomainsettings" title="RStudioServerProDomainSettings">RStudioServerProDomainSettings</a>" : <i><a href="rstudioserverprodomainsettings.md">RStudioServerProDomainSettings</a></i>
}
</pre>

### YAML

<pre>
<a href="#securitygroupids" title="SecurityGroupIds">SecurityGroupIds</a>: <i>
      - String</i>
<a href="#rstudioserverprodomainsettings" title="RStudioServerProDomainSettings">RStudioServerProDomainSettings</a>: <i><a href="rstudioserverprodomainsettings.md">RStudioServerProDomainSettings</a></i>
</pre>

## Properties

#### SecurityGroupIds

The security groups for the Amazon Virtual Private Cloud that the Domain uses for communication between Domain-level apps and user apps.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RStudioServerProDomainSettings

A collection of settings that update the current configuration for the RStudioServerPro Domain-level app.

_Required_: No

_Type_: <a href="rstudioserverprodomainsettings.md">RStudioServerProDomainSettings</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

