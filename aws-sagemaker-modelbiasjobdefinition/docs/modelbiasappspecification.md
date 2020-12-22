# AWS::SageMaker::ModelBiasJobDefinition ModelBiasAppSpecification

Container image configuration object for the monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#imageuri" title="ImageUri">ImageUri</a>" : <i>String</i>,
    "<a href="#configuri" title="ConfigUri">ConfigUri</a>" : <i>String</i>,
    "<a href="#environment" title="Environment">Environment</a>" : <i><a href="modelbiasappspecification-environment.md">Environment</a></i>
}
</pre>

### YAML

<pre>
<a href="#imageuri" title="ImageUri">ImageUri</a>: <i>String</i>
<a href="#configuri" title="ConfigUri">ConfigUri</a>: <i>String</i>
<a href="#environment" title="Environment">Environment</a>: <i><a href="modelbiasappspecification-environment.md">Environment</a></i>
</pre>

## Properties

#### ImageUri

The container image to be run by the monitoring job.

_Required_: Yes

_Type_: String

_Maximum_: <code>255</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ConfigUri

The S3 URI to an analysis configuration file

_Required_: Yes

_Type_: String

_Maximum_: <code>255</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Environment

Sets the environment variables in the Docker container

_Required_: No

_Type_: <a href="modelbiasappspecification-environment.md">Environment</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

