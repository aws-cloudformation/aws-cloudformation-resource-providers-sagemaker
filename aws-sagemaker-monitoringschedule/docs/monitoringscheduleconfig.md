# AWS::SageMaker::MonitoringSchedule MonitoringScheduleConfig

The configuration object that specifies the monitoring schedule and defines the monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#monitoringjobdefinition" title="MonitoringJobDefinition">MonitoringJobDefinition</a>" : <i><a href="monitoringjobdefinition.md">MonitoringJobDefinition</a></i>,
    "<a href="#monitoringjobdefinitionname" title="MonitoringJobDefinitionName">MonitoringJobDefinitionName</a>" : <i>String</i>,
    "<a href="#monitoringtype" title="MonitoringType">MonitoringType</a>" : <i>String</i>,
    "<a href="#scheduleconfig" title="ScheduleConfig">ScheduleConfig</a>" : <i><a href="scheduleconfig.md">ScheduleConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#monitoringjobdefinition" title="MonitoringJobDefinition">MonitoringJobDefinition</a>: <i><a href="monitoringjobdefinition.md">MonitoringJobDefinition</a></i>
<a href="#monitoringjobdefinitionname" title="MonitoringJobDefinitionName">MonitoringJobDefinitionName</a>: <i>String</i>
<a href="#monitoringtype" title="MonitoringType">MonitoringType</a>: <i>String</i>
<a href="#scheduleconfig" title="ScheduleConfig">ScheduleConfig</a>: <i><a href="scheduleconfig.md">ScheduleConfig</a></i>
</pre>

## Properties

#### MonitoringJobDefinition

Defines the monitoring job.

_Required_: No

_Type_: <a href="monitoringjobdefinition.md">MonitoringJobDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringJobDefinitionName

Name of the job definition

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringType

The type of monitoring job.

_Required_: No

_Type_: String

_Allowed Values_: <code>DataQuality</code> | <code>ModelQuality</code> | <code>ModelBias</code> | <code>ModelExplainability</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ScheduleConfig

Configuration details about the monitoring schedule.

_Required_: No

_Type_: <a href="scheduleconfig.md">ScheduleConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

