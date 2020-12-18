# AWS::SageMaker::FeatureGroup OfflineStoreConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#s3storageconfig" title="S3StorageConfig">S3StorageConfig</a>" : <i><a href="s3storageconfig.md">S3StorageConfig</a></i>,
    "<a href="#disablegluetablecreation" title="DisableGlueTableCreation">DisableGlueTableCreation</a>" : <i>Boolean</i>,
    "<a href="#datacatalogconfig" title="DataCatalogConfig">DataCatalogConfig</a>" : <i><a href="datacatalogconfig.md">DataCatalogConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#s3storageconfig" title="S3StorageConfig">S3StorageConfig</a>: <i><a href="s3storageconfig.md">S3StorageConfig</a></i>
<a href="#disablegluetablecreation" title="DisableGlueTableCreation">DisableGlueTableCreation</a>: <i>Boolean</i>
<a href="#datacatalogconfig" title="DataCatalogConfig">DataCatalogConfig</a>: <i><a href="datacatalogconfig.md">DataCatalogConfig</a></i>
</pre>

## Properties

#### S3StorageConfig

_Required_: Yes

_Type_: <a href="s3storageconfig.md">S3StorageConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DisableGlueTableCreation

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DataCatalogConfig

_Required_: No

_Type_: <a href="datacatalogconfig.md">DataCatalogConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

