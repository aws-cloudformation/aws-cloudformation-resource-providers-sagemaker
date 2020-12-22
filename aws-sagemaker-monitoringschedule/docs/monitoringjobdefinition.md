# AWS::SageMaker::MonitoringSchedule MonitoringJobDefinition

Defines the monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#baselineconfig" title="BaselineConfig">BaselineConfig</a>" : <i><a href="baselineconfig.md">BaselineConfig</a></i>,
    "<a href="#environment" title="Environment">Environment</a>" : <i><a href="monitoringjobdefinition-environment.md">Environment</a></i>,
    "<a href="#monitoringappspecification" title="MonitoringAppSpecification">MonitoringAppSpecification</a>" : <i><a href="monitoringappspecification.md">MonitoringAppSpecification</a></i>,
    "<a href="#monitoringinputs" title="MonitoringInputs">MonitoringInputs</a>" : <i>[ <a href="monitoringinput.md">MonitoringInput</a>, ... ]</i>,
    "<a href="#monitoringoutputconfig" title="MonitoringOutputConfig">MonitoringOutputConfig</a>" : <i><a href="monitoringoutputconfig.md">MonitoringOutputConfig</a></i>,
    "<a href="#monitoringresources" title="MonitoringResources">MonitoringResources</a>" : <i><a href="monitoringresources.md">MonitoringResources</a></i>,
    "<a href="#networkconfig" title="NetworkConfig">NetworkConfig</a>" : <i><a href="networkconfig.md">NetworkConfig</a></i>,
    "<a href="#rolearn" title="RoleArn">RoleArn</a>" : <i>String</i>,
    "<a href="#stoppingcondition" title="StoppingCondition">StoppingCondition</a>" : <i><a href="stoppingcondition.md">StoppingCondition</a></i>
}
</pre>

### YAML

<pre>
<a href="#baselineconfig" title="BaselineConfig">BaselineConfig</a>: <i><a href="baselineconfig.md">BaselineConfig</a></i>
<a href="#environment" title="Environment">Environment</a>: <i><a href="monitoringjobdefinition-environment.md">Environment</a></i>
<a href="#monitoringappspecification" title="MonitoringAppSpecification">MonitoringAppSpecification</a>: <i><a href="monitoringappspecification.md">MonitoringAppSpecification</a></i>
<a href="#monitoringinputs" title="MonitoringInputs">MonitoringInputs</a>: <i>
      - <a href="monitoringinput.md">MonitoringInput</a></i>
<a href="#monitoringoutputconfig" title="MonitoringOutputConfig">MonitoringOutputConfig</a>: <i><a href="monitoringoutputconfig.md">MonitoringOutputConfig</a></i>
<a href="#monitoringresources" title="MonitoringResources">MonitoringResources</a>: <i><a href="monitoringresources.md">MonitoringResources</a></i>
<a href="#networkconfig" title="NetworkConfig">NetworkConfig</a>: <i><a href="networkconfig.md">NetworkConfig</a></i>
<a href="#rolearn" title="RoleArn">RoleArn</a>: <i>String</i>
<a href="#stoppingcondition" title="StoppingCondition">StoppingCondition</a>: <i><a href="stoppingcondition.md">StoppingCondition</a></i>
</pre>

## Properties

#### BaselineConfig

Baseline configuration used to validate that the data conforms to the specified constraints and statistics.

_Required_: No

_Type_: <a href="baselineconfig.md">BaselineConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Environment

Sets the environment variables in the Docker container

_Required_: No

_Type_: <a href="monitoringjobdefinition-environment.md">Environment</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringAppSpecification

Container image configuration object for the monitoring job.

_Required_: Yes

_Type_: <a href="monitoringappspecification.md">MonitoringAppSpecification</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringInputs

The array of inputs for the monitoring job.

_Required_: Yes

_Type_: List of <a href="monitoringinput.md">MonitoringInput</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringOutputConfig

The output configuration for monitoring jobs.

_Required_: Yes

_Type_: <a href="monitoringoutputconfig.md">MonitoringOutputConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringResources

Identifies the resources to deploy for a monitoring job.

_Required_: Yes

_Type_: <a href="monitoringresources.md">MonitoringResources</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NetworkConfig

Networking options for a job, such as network traffic encryption between containers, whether to allow inbound and outbound network calls to and from containers, and the VPC subnets and security groups to use for VPC-enabled jobs.

_Required_: No

_Type_: <a href="networkconfig.md">NetworkConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RoleArn

The Amazon Resource Name (ARN) of an IAM role that Amazon SageMaker can assume to perform tasks on your behalf.

_Required_: Yes

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StoppingCondition

Specifies a time limit for how long the monitoring job is allowed to run.

_Required_: No

_Type_: <a href="stoppingcondition.md">StoppingCondition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

