# AWS::SageMaker::DataQualityJobDefinition EndpointInput

The endpoint for a monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#endpointname" title="EndpointName">EndpointName</a>" : <i>String</i>,
    "<a href="#localpath" title="LocalPath">LocalPath</a>" : <i>String</i>,
    "<a href="#s3datadistributiontype" title="S3DataDistributionType">S3DataDistributionType</a>" : <i>String</i>,
    "<a href="#s3inputmode" title="S3InputMode">S3InputMode</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#endpointname" title="EndpointName">EndpointName</a>: <i>String</i>
<a href="#localpath" title="LocalPath">LocalPath</a>: <i>String</i>
<a href="#s3datadistributiontype" title="S3DataDistributionType">S3DataDistributionType</a>: <i>String</i>
<a href="#s3inputmode" title="S3InputMode">S3InputMode</a>: <i>String</i>
</pre>

## Properties

#### EndpointName

The name of the endpoint used to run the monitoring job.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LocalPath

Path to the filesystem where the endpoint data is available to the container.

_Required_: Yes

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3DataDistributionType

Whether input data distributed in Amazon S3 is fully replicated or sharded by an S3 key. Defauts to FullyReplicated

_Required_: No

_Type_: String

_Allowed Values_: <code>FullyReplicated</code> | <code>ShardedByS3Key</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3InputMode

Whether the Pipe or File is used as the input mode for transfering data for the monitoring job. Pipe mode is recommended for large datasets. File mode is useful for small files that fit in memory. Defaults to File.

_Required_: No

_Type_: String

_Allowed Values_: <code>Pipe</code> | <code>File</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

