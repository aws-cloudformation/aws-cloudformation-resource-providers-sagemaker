# AWS::SageMaker::MonitoringSchedule MonitoringAppSpecification

Container image configuration object for the monitoring job.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#containerarguments" title="ContainerArguments">ContainerArguments</a>" : <i>[ String, ... ]</i>,
    "<a href="#containerentrypoint" title="ContainerEntrypoint">ContainerEntrypoint</a>" : <i>[ String, ... ]</i>,
    "<a href="#imageuri" title="ImageUri">ImageUri</a>" : <i>String</i>,
    "<a href="#postanalyticsprocessorsourceuri" title="PostAnalyticsProcessorSourceUri">PostAnalyticsProcessorSourceUri</a>" : <i>String</i>,
    "<a href="#recordpreprocessorsourceuri" title="RecordPreprocessorSourceUri">RecordPreprocessorSourceUri</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#containerarguments" title="ContainerArguments">ContainerArguments</a>: <i>
      - String</i>
<a href="#containerentrypoint" title="ContainerEntrypoint">ContainerEntrypoint</a>: <i>
      - String</i>
<a href="#imageuri" title="ImageUri">ImageUri</a>: <i>String</i>
<a href="#postanalyticsprocessorsourceuri" title="PostAnalyticsProcessorSourceUri">PostAnalyticsProcessorSourceUri</a>: <i>String</i>
<a href="#recordpreprocessorsourceuri" title="RecordPreprocessorSourceUri">RecordPreprocessorSourceUri</a>: <i>String</i>
</pre>

## Properties

#### ContainerArguments

An array of arguments for the container used to run the monitoring job.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ContainerEntrypoint

Specifies the entrypoint for a container used to run the monitoring job.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageUri

The container image to be run by the monitoring job.

_Required_: Yes

_Type_: String

_Maximum_: <code>255</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PostAnalyticsProcessorSourceUri

The Amazon S3 URI.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RecordPreprocessorSourceUri

The Amazon S3 URI.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

