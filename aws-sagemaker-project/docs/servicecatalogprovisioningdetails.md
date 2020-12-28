# AWS::SageMaker::Project ServiceCatalogProvisioningDetails

Input ServiceCatalog Provisioning Details

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#productid" title="ProductId">ProductId</a>" : <i>String</i>,
    "<a href="#provisioningartifactid" title="ProvisioningArtifactId">ProvisioningArtifactId</a>" : <i>String</i>,
    "<a href="#pathid" title="PathId">PathId</a>" : <i>String</i>,
    "<a href="#provisioningparameters" title="ProvisioningParameters">ProvisioningParameters</a>" : <i>[ <a href="provisioningparameter.md">ProvisioningParameter</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#productid" title="ProductId">ProductId</a>: <i>String</i>
<a href="#provisioningartifactid" title="ProvisioningArtifactId">ProvisioningArtifactId</a>: <i>String</i>
<a href="#pathid" title="PathId">PathId</a>: <i>String</i>
<a href="#provisioningparameters" title="ProvisioningParameters">ProvisioningParameters</a>: <i>
      - <a href="provisioningparameter.md">ProvisioningParameter</a></i>
</pre>

## Properties

#### ProductId

Service Catalog product identifier.

_Required_: Yes

_Type_: String

_Maximum_: <code>100</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProvisioningArtifactId

The identifier of the provisioning artifact (also known as a version).

_Required_: Yes

_Type_: String

_Maximum_: <code>100</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PathId

The path identifier of the product.

_Required_: No

_Type_: String

_Maximum_: <code>100</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProvisioningParameters

Parameters specified by the administrator that are required for provisioning the product.

_Required_: No

_Type_: List of <a href="provisioningparameter.md">ProvisioningParameter</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

