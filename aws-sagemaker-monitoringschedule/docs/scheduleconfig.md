# AWS::SageMaker::MonitoringSchedule ScheduleConfig

Configuration details about the monitoring schedule.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#scheduleexpression" title="ScheduleExpression">ScheduleExpression</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#scheduleexpression" title="ScheduleExpression">ScheduleExpression</a>: <i>String</i>
</pre>

## Properties

#### ScheduleExpression

A cron expression that describes details about the monitoring schedule.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

