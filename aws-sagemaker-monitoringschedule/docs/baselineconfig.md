# AWS::SageMaker::MonitoringSchedule BaselineConfig

Baseline configuration used to validate that the data conforms to the specified constraints and statistics.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#constraintsresource" title="ConstraintsResource">ConstraintsResource</a>" : <i><a href="constraintsresource.md">ConstraintsResource</a></i>,
    "<a href="#statisticsresource" title="StatisticsResource">StatisticsResource</a>" : <i><a href="statisticsresource.md">StatisticsResource</a></i>
}
</pre>

### YAML

<pre>
<a href="#constraintsresource" title="ConstraintsResource">ConstraintsResource</a>: <i><a href="constraintsresource.md">ConstraintsResource</a></i>
<a href="#statisticsresource" title="StatisticsResource">StatisticsResource</a>: <i><a href="statisticsresource.md">StatisticsResource</a></i>
</pre>

## Properties

#### ConstraintsResource

The baseline constraints resource for a monitoring job.

_Required_: No

_Type_: <a href="constraintsresource.md">ConstraintsResource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StatisticsResource

The baseline statistics resource for a monitoring job.

_Required_: No

_Type_: <a href="statisticsresource.md">StatisticsResource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

