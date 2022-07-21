# AWS::SageMaker::ModelPackage ModelPackageStatusDetails

Details about the current status of the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#imagescanstatuses" title="ImageScanStatuses">ImageScanStatuses</a>" : <i>[ <a href="modelpackagestatusitem.md">ModelPackageStatusItem</a>, ... ]</i>,
    "<a href="#validationstatuses" title="ValidationStatuses">ValidationStatuses</a>" : <i>[ <a href="modelpackagestatusitem.md">ModelPackageStatusItem</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#imagescanstatuses" title="ImageScanStatuses">ImageScanStatuses</a>: <i>
      - <a href="modelpackagestatusitem.md">ModelPackageStatusItem</a></i>
<a href="#validationstatuses" title="ValidationStatuses">ValidationStatuses</a>: <i>
      - <a href="modelpackagestatusitem.md">ModelPackageStatusItem</a></i>
</pre>

## Properties

#### ImageScanStatuses

_Required_: No

_Type_: List of <a href="modelpackagestatusitem.md">ModelPackageStatusItem</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ValidationStatuses

_Required_: Yes

_Type_: List of <a href="modelpackagestatusitem.md">ModelPackageStatusItem</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

