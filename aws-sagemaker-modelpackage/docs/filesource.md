# AWS::SageMaker::ModelPackage FileSource

Represents a File Source Object.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#contentdigest" title="ContentDigest">ContentDigest</a>" : <i>String</i>,
    "<a href="#contenttype" title="ContentType">ContentType</a>" : <i>String</i>,
    "<a href="#s3uri" title="S3Uri">S3Uri</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#contentdigest" title="ContentDigest">ContentDigest</a>: <i>String</i>
<a href="#contenttype" title="ContentType">ContentType</a>: <i>String</i>
<a href="#s3uri" title="S3Uri">S3Uri</a>: <i>String</i>
</pre>

## Properties

#### ContentDigest

The digest of the file source.

_Required_: No

_Type_: String

_Maximum_: <code>72</code>

_Pattern_: <code>^[Ss][Hh][Aa]256:[0-9a-fA-F]{64}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ContentType

The type of content stored in the file source.

_Required_: No

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3Uri

The Amazon S3 URI for the file source.

_Required_: Yes

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

