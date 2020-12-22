# AWS::SageMaker::MonitoringSchedule MonitoringExecutionSummary

Summary of information about monitoring job

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#creationtime" title="CreationTime">CreationTime</a>" : <i>String</i>,
    "<a href="#endpointname" title="EndpointName">EndpointName</a>" : <i>String</i>,
    "<a href="#failurereason" title="FailureReason">FailureReason</a>" : <i>String</i>,
    "<a href="#lastmodifiedtime" title="LastModifiedTime">LastModifiedTime</a>" : <i>String</i>,
    "<a href="#monitoringexecutionstatus" title="MonitoringExecutionStatus">MonitoringExecutionStatus</a>" : <i>String</i>,
    "<a href="#monitoringschedulename" title="MonitoringScheduleName">MonitoringScheduleName</a>" : <i>String</i>,
    "<a href="#processingjobarn" title="ProcessingJobArn">ProcessingJobArn</a>" : <i>String</i>,
    "<a href="#scheduledtime" title="ScheduledTime">ScheduledTime</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#creationtime" title="CreationTime">CreationTime</a>: <i>String</i>
<a href="#endpointname" title="EndpointName">EndpointName</a>: <i>String</i>
<a href="#failurereason" title="FailureReason">FailureReason</a>: <i>String</i>
<a href="#lastmodifiedtime" title="LastModifiedTime">LastModifiedTime</a>: <i>String</i>
<a href="#monitoringexecutionstatus" title="MonitoringExecutionStatus">MonitoringExecutionStatus</a>: <i>String</i>
<a href="#monitoringschedulename" title="MonitoringScheduleName">MonitoringScheduleName</a>: <i>String</i>
<a href="#processingjobarn" title="ProcessingJobArn">ProcessingJobArn</a>: <i>String</i>
<a href="#scheduledtime" title="ScheduledTime">ScheduledTime</a>: <i>String</i>
</pre>

## Properties

#### CreationTime

The time at which the monitoring job was created.

_Required_: Yes

_Type_: String

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

_Maximum_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastModifiedTime

A timestamp that indicates the last time the monitoring job was modified.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringExecutionStatus

The status of the monitoring job.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>Pending</code> | <code>Completed</code> | <code>CompletedWithViolations</code> | <code>InProgress</code> | <code>Failed</code> | <code>Stopping</code> | <code>Stopped</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringScheduleName

The name of the monitoring schedule.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProcessingJobArn

The Amazon Resource Name (ARN) of the monitoring job.

_Required_: No

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>aws[a-z\-]*:sagemaker:[a-z0-9\-]*:[0-9]{12}:processing-job/.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ScheduledTime

The time the monitoring job was scheduled.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

