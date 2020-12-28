# AWS::SageMaker::ModelBiasJobDefinition

Resource Type definition for AWS::SageMaker::ModelBiasJobDefinition

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::ModelBiasJobDefinition",
    "Properties" : {
        "<a href="#jobdefinitionname" title="JobDefinitionName">JobDefinitionName</a>" : <i>String</i>,
        "<a href="#modelbiasbaselineconfig" title="ModelBiasBaselineConfig">ModelBiasBaselineConfig</a>" : <i><a href="modelbiasbaselineconfig.md">ModelBiasBaselineConfig</a></i>,
        "<a href="#modelbiasappspecification" title="ModelBiasAppSpecification">ModelBiasAppSpecification</a>" : <i><a href="modelbiasappspecification.md">ModelBiasAppSpecification</a></i>,
        "<a href="#modelbiasjobinput" title="ModelBiasJobInput">ModelBiasJobInput</a>" : <i><a href="modelbiasjobinput.md">ModelBiasJobInput</a></i>,
        "<a href="#modelbiasjoboutputconfig" title="ModelBiasJobOutputConfig">ModelBiasJobOutputConfig</a>" : <i><a href="monitoringoutputconfig.md">MonitoringOutputConfig</a></i>,
        "<a href="#jobresources" title="JobResources">JobResources</a>" : <i><a href="monitoringresources.md">MonitoringResources</a></i>,
        "<a href="#networkconfig" title="NetworkConfig">NetworkConfig</a>" : <i><a href="networkconfig.md">NetworkConfig</a></i>,
        "<a href="#rolearn" title="RoleArn">RoleArn</a>" : <i>String</i>,
        "<a href="#stoppingcondition" title="StoppingCondition">StoppingCondition</a>" : <i><a href="stoppingcondition.md">StoppingCondition</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::ModelBiasJobDefinition
Properties:
    <a href="#jobdefinitionname" title="JobDefinitionName">JobDefinitionName</a>: <i>String</i>
    <a href="#modelbiasbaselineconfig" title="ModelBiasBaselineConfig">ModelBiasBaselineConfig</a>: <i><a href="modelbiasbaselineconfig.md">ModelBiasBaselineConfig</a></i>
    <a href="#modelbiasappspecification" title="ModelBiasAppSpecification">ModelBiasAppSpecification</a>: <i><a href="modelbiasappspecification.md">ModelBiasAppSpecification</a></i>
    <a href="#modelbiasjobinput" title="ModelBiasJobInput">ModelBiasJobInput</a>: <i><a href="modelbiasjobinput.md">ModelBiasJobInput</a></i>
    <a href="#modelbiasjoboutputconfig" title="ModelBiasJobOutputConfig">ModelBiasJobOutputConfig</a>: <i><a href="monitoringoutputconfig.md">MonitoringOutputConfig</a></i>
    <a href="#jobresources" title="JobResources">JobResources</a>: <i><a href="monitoringresources.md">MonitoringResources</a></i>
    <a href="#networkconfig" title="NetworkConfig">NetworkConfig</a>: <i><a href="networkconfig.md">NetworkConfig</a></i>
    <a href="#rolearn" title="RoleArn">RoleArn</a>: <i>String</i>
    <a href="#stoppingcondition" title="StoppingCondition">StoppingCondition</a>: <i><a href="stoppingcondition.md">StoppingCondition</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### JobDefinitionName

The name of the job definition.

_Required_: No

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelBiasBaselineConfig

Baseline configuration used to validate that the data conforms to the specified constraints and statistics.

_Required_: No

_Type_: <a href="modelbiasbaselineconfig.md">ModelBiasBaselineConfig</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelBiasAppSpecification

Container image configuration object for the monitoring job.

_Required_: Yes

_Type_: <a href="modelbiasappspecification.md">ModelBiasAppSpecification</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelBiasJobInput

The inputs for a monitoring job.

_Required_: Yes

_Type_: <a href="modelbiasjobinput.md">ModelBiasJobInput</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelBiasJobOutputConfig

The output configuration for monitoring jobs.

_Required_: Yes

_Type_: <a href="monitoringoutputconfig.md">MonitoringOutputConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### JobResources

Identifies the resources to deploy for a monitoring job.

_Required_: Yes

_Type_: <a href="monitoringresources.md">MonitoringResources</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NetworkConfig

Networking options for a job, such as network traffic encryption between containers, whether to allow inbound and outbound network calls to and from containers, and the VPC subnets and security groups to use for VPC-enabled jobs.

_Required_: No

_Type_: <a href="networkconfig.md">NetworkConfig</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### RoleArn

The Amazon Resource Name (ARN) of an IAM role that Amazon SageMaker can assume to perform tasks on your behalf.

_Required_: Yes

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### StoppingCondition

Specifies a time limit for how long the monitoring job is allowed to run.

_Required_: No

_Type_: <a href="stoppingcondition.md">StoppingCondition</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the JobDefinitionArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### CreationTime

The time at which the job definition was created.

#### JobDefinitionArn

The Amazon Resource Name (ARN) of job definition.

