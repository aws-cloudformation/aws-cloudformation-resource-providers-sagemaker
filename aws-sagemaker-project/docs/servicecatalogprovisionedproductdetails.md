# AWS::SageMaker::Project ServiceCatalogProvisionedProductDetails

Provisioned ServiceCatalog  Details

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#provisionedproductid" title="ProvisionedProductId">ProvisionedProductId</a>" : <i>String</i>,
    "<a href="#provisionedproductstatusmessage" title="ProvisionedProductStatusMessage">ProvisionedProductStatusMessage</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#provisionedproductid" title="ProvisionedProductId">ProvisionedProductId</a>: <i>String</i>
<a href="#provisionedproductstatusmessage" title="ProvisionedProductStatusMessage">ProvisionedProductStatusMessage</a>: <i>String</i>
</pre>

## Properties

#### ProvisionedProductId

The identifier of the provisioning artifact (also known as a version).

_Required_: No

_Type_: String

_Maximum_: <code>100</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProvisionedProductStatusMessage

Provisioned Product Status Message

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

