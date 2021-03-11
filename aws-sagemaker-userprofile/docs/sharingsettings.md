# AWS::SageMaker::UserProfile SharingSettings

Specifies options when sharing an Amazon SageMaker Studio notebook. These settings are specified as part of DefaultUserSettings when the CreateDomain API is called, and as part of UserSettings when the CreateUserProfile API is called.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#notebookoutputoption" title="NotebookOutputOption">NotebookOutputOption</a>" : <i>String</i>,
    "<a href="#s3kmskeyid" title="S3KmsKeyId">S3KmsKeyId</a>" : <i>String</i>,
    "<a href="#s3outputpath" title="S3OutputPath">S3OutputPath</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#notebookoutputoption" title="NotebookOutputOption">NotebookOutputOption</a>: <i>String</i>
<a href="#s3kmskeyid" title="S3KmsKeyId">S3KmsKeyId</a>: <i>String</i>
<a href="#s3outputpath" title="S3OutputPath">S3OutputPath</a>: <i>String</i>
</pre>

## Properties

#### NotebookOutputOption

Whether to include the notebook cell output when sharing the notebook. The default is Disabled.

_Required_: No

_Type_: String

_Allowed Values_: <code>Allowed</code> | <code>Disabled</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3KmsKeyId

When NotebookOutputOption is Allowed, the AWS Key Management Service (KMS) encryption key ID used to encrypt the notebook cell output in the Amazon S3 bucket.

_Required_: No

_Type_: String

_Maximum_: <code>2048</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3OutputPath

When NotebookOutputOption is Allowed, the Amazon S3 bucket used to store the shared notebook snapshots.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

