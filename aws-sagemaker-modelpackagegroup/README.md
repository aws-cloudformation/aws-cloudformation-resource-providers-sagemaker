# AWS::SageMaker::ModelPackageGroup

Resource Type definition for AWS::SageMaker::ModelPackageGroup

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::ModelPackageGroup",
    "Properties" : {
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#modelpackagegrouparn" title="ModelPackageGroupArn">ModelPackageGroupArn</a>" : <i>String</i>,
        "<a href="#modelpackagegroupname" title="ModelPackageGroupName">ModelPackageGroupName</a>" : <i>String</i>,
        "<a href="#modelpackagegroupdescription" title="ModelPackageGroupDescription">ModelPackageGroupDescription</a>" : <i>String</i>,
        "<a href="#modelpackagegrouppolicy" title="ModelPackageGroupPolicy">ModelPackageGroupPolicy</a>" : <i>Map</i>,
        "<a href="#creationtime" title="CreationTime">CreationTime</a>" : <i>String</i>,
        "<a href="#modelpackagegroupstatus" title="ModelPackageGroupStatus">ModelPackageGroupStatus</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::ModelPackageGroup
Properties:
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#modelpackagegrouparn" title="ModelPackageGroupArn">ModelPackageGroupArn</a>: <i>String</i>
    <a href="#modelpackagegroupname" title="ModelPackageGroupName">ModelPackageGroupName</a>: <i>String</i>
    <a href="#modelpackagegroupdescription" title="ModelPackageGroupDescription">ModelPackageGroupDescription</a>: <i>String</i>
    <a href="#modelpackagegrouppolicy" title="ModelPackageGroupPolicy">ModelPackageGroupPolicy</a>: <i>Map</i>
    <a href="#creationtime" title="CreationTime">CreationTime</a>: <i>String</i>
    <a href="#modelpackagegroupstatus" title="ModelPackageGroupStatus">ModelPackageGroupStatus</a>: <i>String</i>
</pre>

## Properties

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageGroupArn

The Amazon Resource Name (ARN) of the model package group.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>arn:.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageGroupName

The name of the model package group.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageGroupDescription

The description of the model package group.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>[\p{L}\p{M}\p{Z}\p{S}\p{N}\p{P}]*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageGroupPolicy

_Required_: No

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CreationTime

The time at which the model package group was created.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageGroupStatus

The status of a modelpackage group job.

_Required_: No

_Type_: String

_Allowed Values_: <code>Pending</code> | <code>InProgress</code> | <code>Completed</code> | <code>Failed</code> | <code>Deleting</code> | <code>DeleteFailed</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ModelPackageGroupArn.
