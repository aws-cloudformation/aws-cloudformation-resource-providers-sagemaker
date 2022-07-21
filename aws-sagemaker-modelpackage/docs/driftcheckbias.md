# AWS::SageMaker::ModelPackage DriftCheckBias

Represents the drift check bias baselines that can be used when the model monitor is set using the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#posttrainingconstraints" title="PostTrainingConstraints">PostTrainingConstraints</a>" : <i><a href="metricssource.md">MetricsSource</a></i>,
    "<a href="#pretrainingconstraints" title="PreTrainingConstraints">PreTrainingConstraints</a>" : <i><a href="metricssource.md">MetricsSource</a></i>,
    "<a href="#configfile" title="ConfigFile">ConfigFile</a>" : <i><a href="filesource.md">FileSource</a></i>
}
</pre>

### YAML

<pre>
<a href="#posttrainingconstraints" title="PostTrainingConstraints">PostTrainingConstraints</a>: <i><a href="metricssource.md">MetricsSource</a></i>
<a href="#pretrainingconstraints" title="PreTrainingConstraints">PreTrainingConstraints</a>: <i><a href="metricssource.md">MetricsSource</a></i>
<a href="#configfile" title="ConfigFile">ConfigFile</a>: <i><a href="filesource.md">FileSource</a></i>
</pre>

## Properties

#### PostTrainingConstraints

Represents a Metric Source Object.

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PreTrainingConstraints

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ConfigFile

Represents a File Source Object.

_Required_: No

_Type_: <a href="filesource.md">FileSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

