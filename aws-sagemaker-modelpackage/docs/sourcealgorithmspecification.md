# AWS::SageMaker::ModelPackage SourceAlgorithmSpecification

Details about the algorithm that was used to create the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#sourcealgorithms" title="SourceAlgorithms">SourceAlgorithms</a>" : <i>[ <a href="sourcealgorithm.md">SourceAlgorithm</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#sourcealgorithms" title="SourceAlgorithms">SourceAlgorithms</a>: <i>
      - <a href="sourcealgorithm.md">SourceAlgorithm</a></i>
</pre>

## Properties

#### SourceAlgorithms

A list of algorithms that were used to create a model package.

_Required_: Yes

_Type_: List of <a href="sourcealgorithm.md">SourceAlgorithm</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

