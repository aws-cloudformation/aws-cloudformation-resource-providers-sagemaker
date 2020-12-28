# AWS::SageMaker::Pipeline S3Location

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#bucket" title="Bucket">Bucket</a>" : <i>String</i>,
    "<a href="#key" title="Key">Key</a>" : <i>String</i>,
    "<a href="#version" title="Version">Version</a>" : <i>String</i>,
    "<a href="#etag" title="ETag">ETag</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#bucket" title="Bucket">Bucket</a>: <i>String</i>
<a href="#key" title="Key">Key</a>: <i>String</i>
<a href="#version" title="Version">Version</a>: <i>String</i>
<a href="#etag" title="ETag">ETag</a>: <i>String</i>
</pre>

## Properties

#### Bucket

The name of the S3 bucket where the PipelineDefinition file is stored.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Key

The file name of the PipelineDefinition file (Amazon S3 object name).

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Version

For versioning-enabled buckets, a specific version of the PipelineDefinition file.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ETag

The Amazon S3 ETag (a file checksum) of the PipelineDefinition file. If you don't specify a value, SageMaker skips ETag validation of your PipelineDefinition file.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

