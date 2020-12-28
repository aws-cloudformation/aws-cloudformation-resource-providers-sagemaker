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
        "<a href="#modelpackagegroupname" title="ModelPackageGroupName">ModelPackageGroupName</a>" : <i>String</i>,
        "<a href="#modelpackagegroupdescription" title="ModelPackageGroupDescription">ModelPackageGroupDescription</a>" : <i>String</i>,
        "<a href="#modelpackagegrouppolicy" title="ModelPackageGroupPolicy">ModelPackageGroupPolicy</a>" : <i>Map, String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::ModelPackageGroup
Properties:
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#modelpackagegroupname" title="ModelPackageGroupName">ModelPackageGroupName</a>: <i>String</i>
    <a href="#modelpackagegroupdescription" title="ModelPackageGroupDescription">ModelPackageGroupDescription</a>: <i>String</i>
    <a href="#modelpackagegrouppolicy" title="ModelPackageGroupPolicy">ModelPackageGroupPolicy</a>: <i>Map, String</i>
</pre>

## Properties

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageGroupName

The name of the model package group.

_Required_: Yes

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelPackageGroupDescription

The description of the model package group.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>[\p{L}\p{M}\p{Z}\p{S}\p{N}\p{P}]*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelPackageGroupPolicy

_Required_: No

_Type_: Map, String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ModelPackageGroupArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ModelPackageGroupArn

The Amazon Resource Name (ARN) of the model package group.

#### CreationTime

The time at which the model package group was created.

#### ModelPackageGroupStatus

The status of a modelpackage group job.

