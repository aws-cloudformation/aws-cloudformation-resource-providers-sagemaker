# AWS::SageMaker::ModelPackage S3DataSource

Describes the S3 data source.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#s3datatype" title="S3DataType">S3DataType</a>" : <i>String</i>,
    "<a href="#s3uri" title="S3Uri">S3Uri</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#s3datatype" title="S3DataType">S3DataType</a>: <i>String</i>
<a href="#s3uri" title="S3Uri">S3Uri</a>: <i>String</i>
</pre>

## Properties

#### S3DataType

The S3 Data Source Type

_Required_: Yes

_Type_: String

_Allowed Values_: <code>ManifestFile</code> | <code>S3Prefix</code> | <code>AugmentedManifestFile</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3Uri

Depending on the value specified for the S3DataType, identifies either a key name prefix or a manifest.

_Required_: Yes

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

