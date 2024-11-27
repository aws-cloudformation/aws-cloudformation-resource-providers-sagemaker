# AWS::SageMaker::ModelPackage TransformInput

Describes the input source of a transform job and the way the transform job consumes it.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#compressiontype" title="CompressionType">CompressionType</a>" : <i>String</i>,
    "<a href="#contenttype" title="ContentType">ContentType</a>" : <i>String</i>,
    "<a href="#datasource" title="DataSource">DataSource</a>" : <i><a href="datasource.md">DataSource</a></i>,
    "<a href="#splittype" title="SplitType">SplitType</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#compressiontype" title="CompressionType">CompressionType</a>: <i>String</i>
<a href="#contenttype" title="ContentType">ContentType</a>: <i>String</i>
<a href="#datasource" title="DataSource">DataSource</a>: <i><a href="datasource.md">DataSource</a></i>
<a href="#splittype" title="SplitType">SplitType</a>: <i>String</i>
</pre>

## Properties

#### CompressionType

If your transform data is compressed, specify the compression type. Amazon SageMaker automatically decompresses the data for the transform job accordingly. The default value is None.

_Required_: No

_Type_: String

_Allowed Values_: <code>None</code> | <code>Gzip</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ContentType

The multipurpose internet mail extension (MIME) type of the data. Amazon SageMaker uses the MIME type with each http call to transfer data to the transform job.

_Required_: No

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DataSource

Describes the input source of a transform job and the way the transform job consumes it.

_Required_: Yes

_Type_: <a href="datasource.md">DataSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SplitType

The method to use to split the transform job's data files into smaller batches. 

_Required_: No

_Type_: String

_Allowed Values_: <code>None</code> | <code>TFRecord</code> | <code>Line</code> | <code>RecordIO</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

