# AWS::SageMaker::ModelPackage DriftCheckBaselines

Represents the drift check baselines that can be used when the model monitor is set using the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#bias" title="Bias">Bias</a>" : <i><a href="driftcheckbias.md">DriftCheckBias</a></i>,
    "<a href="#explainability" title="Explainability">Explainability</a>" : <i><a href="driftcheckexplainability.md">DriftCheckExplainability</a></i>,
    "<a href="#modeldataquality" title="ModelDataQuality">ModelDataQuality</a>" : <i><a href="driftcheckmodeldataquality.md">DriftCheckModelDataQuality</a></i>,
    "<a href="#modelquality" title="ModelQuality">ModelQuality</a>" : <i><a href="driftcheckmodelquality.md">DriftCheckModelQuality</a></i>
}
</pre>

### YAML

<pre>
<a href="#bias" title="Bias">Bias</a>: <i><a href="driftcheckbias.md">DriftCheckBias</a></i>
<a href="#explainability" title="Explainability">Explainability</a>: <i><a href="driftcheckexplainability.md">DriftCheckExplainability</a></i>
<a href="#modeldataquality" title="ModelDataQuality">ModelDataQuality</a>: <i><a href="driftcheckmodeldataquality.md">DriftCheckModelDataQuality</a></i>
<a href="#modelquality" title="ModelQuality">ModelQuality</a>: <i><a href="driftcheckmodelquality.md">DriftCheckModelQuality</a></i>
</pre>

## Properties

#### Bias

Represents the drift check bias baselines that can be used when the model monitor is set using the model package.

_Required_: No

_Type_: <a href="driftcheckbias.md">DriftCheckBias</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Explainability

Contains explainability metrics for a model.

_Required_: No

_Type_: <a href="driftcheckexplainability.md">DriftCheckExplainability</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelDataQuality

Represents the drift check data quality baselines that can be used when the model monitor is set using the model package.

_Required_: No

_Type_: <a href="driftcheckmodeldataquality.md">DriftCheckModelDataQuality</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelQuality

Represents the drift check model quality baselines that can be used when the model monitor is set using the model package.

_Required_: No

_Type_: <a href="driftcheckmodelquality.md">DriftCheckModelQuality</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

