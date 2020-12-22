# AWS::SageMaker::MonitoringSchedule S3Output

Information about where and how to store the results of a monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#localpath" title="LocalPath">LocalPath</a>" : <i>String</i>,
    "<a href="#s3uploadmode" title="S3UploadMode">S3UploadMode</a>" : <i>String</i>,
    "<a href="#s3uri" title="S3Uri">S3Uri</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#localpath" title="LocalPath">LocalPath</a>: <i>String</i>
<a href="#s3uploadmode" title="S3UploadMode">S3UploadMode</a>: <i>String</i>
<a href="#s3uri" title="S3Uri">S3Uri</a>: <i>String</i>
</pre>

## Properties

#### LocalPath

The local path to the Amazon S3 storage location where Amazon SageMaker saves the results of a monitoring job. LocalPath is an absolute path for the output data.

_Required_: Yes

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3UploadMode

Whether to upload the results of the monitoring job continuously or after the job completes.

_Required_: No

_Type_: String

_Allowed Values_: <code>Continuous</code> | <code>EndOfJob</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3Uri

A URI that identifies the Amazon S3 storage location where Amazon SageMaker saves the results of a monitoring job.

_Required_: Yes

_Type_: String

_Maximum_: <code>512</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

