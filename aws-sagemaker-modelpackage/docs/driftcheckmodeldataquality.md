# AWS::SageMaker::ModelPackage DriftCheckModelDataQuality

Represents the drift check data quality baselines that can be used when the model monitor is set using the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#constraints" title="Constraints">Constraints</a>" : <i><a href="metricssource.md">MetricsSource</a></i>,
    "<a href="#statistics" title="Statistics">Statistics</a>" : <i><a href="metricssource.md">MetricsSource</a></i>
}
</pre>

### YAML

<pre>
<a href="#constraints" title="Constraints">Constraints</a>: <i><a href="metricssource.md">MetricsSource</a></i>
<a href="#statistics" title="Statistics">Statistics</a>: <i><a href="metricssource.md">MetricsSource</a></i>
</pre>

## Properties

#### Constraints

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Statistics

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

