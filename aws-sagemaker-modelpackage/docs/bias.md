# AWS::SageMaker::ModelPackage Bias

Contains bias metrics for a model.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#report" title="Report">Report</a>" : <i><a href="metricssource.md">MetricsSource</a></i>,
    "<a href="#pretrainingreport" title="PreTrainingReport">PreTrainingReport</a>" : <i><a href="metricssource.md">MetricsSource</a></i>,
    "<a href="#posttrainingreport" title="PostTrainingReport">PostTrainingReport</a>" : <i><a href="metricssource.md">MetricsSource</a></i>
}
</pre>

### YAML

<pre>
<a href="#report" title="Report">Report</a>: <i><a href="metricssource.md">MetricsSource</a></i>
<a href="#pretrainingreport" title="PreTrainingReport">PreTrainingReport</a>: <i><a href="metricssource.md">MetricsSource</a></i>
<a href="#posttrainingreport" title="PostTrainingReport">PostTrainingReport</a>: <i><a href="metricssource.md">MetricsSource</a></i>
</pre>

## Properties

#### Report

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PreTrainingReport

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PostTrainingReport

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

