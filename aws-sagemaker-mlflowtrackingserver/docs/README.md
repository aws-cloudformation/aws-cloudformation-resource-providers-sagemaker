# AWS::SageMaker::MlflowTrackingServer

Resource Type definition for AWS::SageMaker::MlflowTrackingServer

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::MlflowTrackingServer",
    "Properties" : {
        "<a href="#trackingservername" title="TrackingServerName">TrackingServerName</a>" : <i>String</i>,
        "<a href="#trackingserversize" title="TrackingServerSize">TrackingServerSize</a>" : <i>String</i>,
        "<a href="#mlflowversion" title="MlflowVersion">MlflowVersion</a>" : <i>String</i>,
        "<a href="#rolearn" title="RoleArn">RoleArn</a>" : <i>String</i>,
        "<a href="#artifactstoreuri" title="ArtifactStoreUri">ArtifactStoreUri</a>" : <i>String</i>,
        "<a href="#automaticmodelregistration" title="AutomaticModelRegistration">AutomaticModelRegistration</a>" : <i>Boolean</i>,
        "<a href="#weeklymaintenancewindowstart" title="WeeklyMaintenanceWindowStart">WeeklyMaintenanceWindowStart</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::MlflowTrackingServer
Properties:
    <a href="#trackingservername" title="TrackingServerName">TrackingServerName</a>: <i>String</i>
    <a href="#trackingserversize" title="TrackingServerSize">TrackingServerSize</a>: <i>String</i>
    <a href="#mlflowversion" title="MlflowVersion">MlflowVersion</a>: <i>String</i>
    <a href="#rolearn" title="RoleArn">RoleArn</a>: <i>String</i>
    <a href="#artifactstoreuri" title="ArtifactStoreUri">ArtifactStoreUri</a>: <i>String</i>
    <a href="#automaticmodelregistration" title="AutomaticModelRegistration">AutomaticModelRegistration</a>: <i>Boolean</i>
    <a href="#weeklymaintenancewindowstart" title="WeeklyMaintenanceWindowStart">WeeklyMaintenanceWindowStart</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### TrackingServerName

The name of the MLFlow Tracking Server.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>256</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,255}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### TrackingServerSize

The size of the MLFlow Tracking Server.

_Required_: No

_Type_: String

_Allowed Values_: <code>Small</code> | <code>Medium</code> | <code>Large</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MlflowVersion

The MLFlow Version used on the MLFlow Tracking Server.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>32</code>

_Pattern_: <code>^\d+(\.\d+)+$</code>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### RoleArn

The Amazon Resource Name (ARN) of an IAM role that enables Amazon SageMaker to perform tasks on behalf of the customer.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role\/?[a-zA-Z_0-9+=,.@\-_\/]+$</code>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### ArtifactStoreUri

The Amazon S3 URI for MLFlow Tracking Server artifacts.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^s3:\/\/([^\/]+)\/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutomaticModelRegistration

A flag to enable Automatic SageMaker Model Registration.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WeeklyMaintenanceWindowStart

The start of the time window for maintenance of the MLFlow Tracking Server in UTC time.

_Required_: No

_Type_: String

_Maximum Length_: <code>9</code>

_Pattern_: <code>^(Mon|Tue|Wed|Thu|Fri|Sat|Sun):([01]\d|2[0-3]):([0-5]\d)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the TrackingServerName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### TrackingServerArn

The Amazon Resource Name (ARN) of the MLFlow Tracking Server.

