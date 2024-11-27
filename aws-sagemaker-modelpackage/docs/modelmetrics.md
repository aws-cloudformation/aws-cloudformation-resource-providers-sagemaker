# AWS::SageMaker::ModelPackage ModelMetrics

A structure that contains model metrics reports.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#bias" title="Bias">Bias</a>" : <i><a href="bias.md">Bias</a></i>,
    "<a href="#explainability" title="Explainability">Explainability</a>" : <i><a href="explainability.md">Explainability</a></i>,
    "<a href="#modeldataquality" title="ModelDataQuality">ModelDataQuality</a>" : <i><a href="modeldataquality.md">ModelDataQuality</a></i>,
    "<a href="#modelquality" title="ModelQuality">ModelQuality</a>" : <i><a href="modelquality.md">ModelQuality</a></i>
}
</pre>

### YAML

<pre>
<a href="#bias" title="Bias">Bias</a>: <i><a href="bias.md">Bias</a></i>
<a href="#explainability" title="Explainability">Explainability</a>: <i><a href="explainability.md">Explainability</a></i>
<a href="#modeldataquality" title="ModelDataQuality">ModelDataQuality</a>: <i><a href="modeldataquality.md">ModelDataQuality</a></i>
<a href="#modelquality" title="ModelQuality">ModelQuality</a>: <i><a href="modelquality.md">ModelQuality</a></i>
</pre>

## Properties

#### Bias

Contains bias metrics for a model.

_Required_: No

_Type_: <a href="bias.md">Bias</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Explainability

Contains explainability metrics for a model.

_Required_: No

_Type_: <a href="explainability.md">Explainability</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelDataQuality

Metrics that measure the quality of the input data for a model.

_Required_: No

_Type_: <a href="modeldataquality.md">ModelDataQuality</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelQuality

Metrics that measure the quality of a model.

_Required_: No

_Type_: <a href="modelquality.md">ModelQuality</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

