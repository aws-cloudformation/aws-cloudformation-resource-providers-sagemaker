# AWS::SageMaker::MonitoringSchedule

Resource Type definition for AWS::SageMaker::MonitoringSchedule

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::MonitoringSchedule",
    "Properties" : {
        "<a href="#monitoringschedulename" title="MonitoringScheduleName">MonitoringScheduleName</a>" : <i>String</i>,
        "<a href="#monitoringscheduleconfig" title="MonitoringScheduleConfig">MonitoringScheduleConfig</a>" : <i><a href="monitoringscheduleconfig.md">MonitoringScheduleConfig</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#endpointname" title="EndpointName">EndpointName</a>" : <i>String</i>,
        "<a href="#failurereason" title="FailureReason">FailureReason</a>" : <i>String</i>,
        "<a href="#lastmonitoringexecutionsummary" title="LastMonitoringExecutionSummary">LastMonitoringExecutionSummary</a>" : <i><a href="monitoringexecutionsummary.md">MonitoringExecutionSummary</a></i>,
        "<a href="#monitoringschedulestatus" title="MonitoringScheduleStatus">MonitoringScheduleStatus</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::MonitoringSchedule
Properties:
    <a href="#monitoringschedulename" title="MonitoringScheduleName">MonitoringScheduleName</a>: <i>String</i>
    <a href="#monitoringscheduleconfig" title="MonitoringScheduleConfig">MonitoringScheduleConfig</a>: <i><a href="monitoringscheduleconfig.md">MonitoringScheduleConfig</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#endpointname" title="EndpointName">EndpointName</a>: <i>String</i>
    <a href="#failurereason" title="FailureReason">FailureReason</a>: <i>String</i>
    <a href="#lastmonitoringexecutionsummary" title="LastMonitoringExecutionSummary">LastMonitoringExecutionSummary</a>: <i><a href="monitoringexecutionsummary.md">MonitoringExecutionSummary</a></i>
    <a href="#monitoringschedulestatus" title="MonitoringScheduleStatus">MonitoringScheduleStatus</a>: <i>String</i>
</pre>

## Properties

#### MonitoringScheduleName

The name of the monitoring schedule.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### MonitoringScheduleConfig

The configuration object that specifies the monitoring schedule and defines the monitoring job.

_Required_: Yes

_Type_: <a href="monitoringscheduleconfig.md">MonitoringScheduleConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndpointName

The name of the endpoint used to run the monitoring job.

_Required_: No

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FailureReason

Contains the reason a monitoring job failed, if it failed.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastMonitoringExecutionSummary

Summary of information about monitoring job

_Required_: No

_Type_: <a href="monitoringexecutionsummary.md">MonitoringExecutionSummary</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringScheduleStatus

The status of a schedule job.

_Required_: No

_Type_: String

_Allowed Values_: <code>Pending</code> | <code>Failed</code> | <code>Scheduled</code> | <code>Stopped</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the MonitoringScheduleArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### MonitoringScheduleArn

The Amazon Resource Name (ARN) of the monitoring schedule.

#### CreationTime

The time at which the schedule was created.

#### LastModifiedTime

A timestamp that indicates the last time the monitoring job was modified.

