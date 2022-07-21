# AWS::SageMaker::ModelPackage DriftCheckExplainability

Contains explainability metrics for a model.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#constraints" title="Constraints">Constraints</a>" : <i><a href="metricssource.md">MetricsSource</a></i>,
    "<a href="#configfile" title="ConfigFile">ConfigFile</a>" : <i><a href="filesource.md">FileSource</a></i>
}
</pre>

### YAML

<pre>
<a href="#constraints" title="Constraints">Constraints</a>: <i><a href="metricssource.md">MetricsSource</a></i>
<a href="#configfile" title="ConfigFile">ConfigFile</a>: <i><a href="filesource.md">FileSource</a></i>
</pre>

## Properties

#### Constraints

_Required_: No

_Type_: <a href="metricssource.md">MetricsSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ConfigFile

_Required_: No

_Type_: <a href="filesource.md">FileSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

