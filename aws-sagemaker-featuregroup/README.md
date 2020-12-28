# AWS::SageMaker::FeatureGroup

Resource Type definition for AWS::SageMaker::FeatureGroup

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::FeatureGroup",
    "Properties" : {
        "<a href="#featuregroupname" title="FeatureGroupName">FeatureGroupName</a>" : <i>String</i>,
        "<a href="#recordidentifierfeaturename" title="RecordIdentifierFeatureName">RecordIdentifierFeatureName</a>" : <i>String</i>,
        "<a href="#eventtimefeaturename" title="EventTimeFeatureName">EventTimeFeatureName</a>" : <i>String</i>,
        "<a href="#featuredefinitions" title="FeatureDefinitions">FeatureDefinitions</a>" : <i>[ <a href="featuredefinition.md">FeatureDefinition</a>, ... ]</i>,
        "<a href="#onlinestoreconfig" title="OnlineStoreConfig">OnlineStoreConfig</a>" : <i><a href="onlinestoreconfig.md">OnlineStoreConfig</a></i>,
        "<a href="#offlinestoreconfig" title="OfflineStoreConfig">OfflineStoreConfig</a>" : <i><a href="offlinestoreconfig.md">OfflineStoreConfig</a></i>,
        "<a href="#rolearn" title="RoleArn">RoleArn</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::FeatureGroup
Properties:
    <a href="#featuregroupname" title="FeatureGroupName">FeatureGroupName</a>: <i>String</i>
    <a href="#recordidentifierfeaturename" title="RecordIdentifierFeatureName">RecordIdentifierFeatureName</a>: <i>String</i>
    <a href="#eventtimefeaturename" title="EventTimeFeatureName">EventTimeFeatureName</a>: <i>String</i>
    <a href="#featuredefinitions" title="FeatureDefinitions">FeatureDefinitions</a>: <i>
      - <a href="featuredefinition.md">FeatureDefinition</a></i>
    <a href="#onlinestoreconfig" title="OnlineStoreConfig">OnlineStoreConfig</a>: <i><a href="onlinestoreconfig.md">OnlineStoreConfig</a></i>
    <a href="#offlinestoreconfig" title="OfflineStoreConfig">OfflineStoreConfig</a>: <i><a href="offlinestoreconfig.md">OfflineStoreConfig</a></i>
    <a href="#rolearn" title="RoleArn">RoleArn</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### FeatureGroupName

The Name of the FeatureGroup.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,63}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### RecordIdentifierFeatureName

The Record Identifier Feature Name.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,63}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EventTimeFeatureName

The Event Time Feature Name.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,63}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### FeatureDefinitions

An Array of Feature Definition

_Required_: Yes

_Type_: List of <a href="featuredefinition.md">FeatureDefinition</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### OnlineStoreConfig

_Required_: No

_Type_: <a href="onlinestoreconfig.md">OnlineStoreConfig</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### OfflineStoreConfig

_Required_: No

_Type_: <a href="offlinestoreconfig.md">OfflineStoreConfig</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### RoleArn

Role Arn

_Required_: No

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Description

Description about the FeatureGroup.

_Required_: No

_Type_: String

_Maximum_: <code>128</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

An array of key-value pair to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the FeatureGroupName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### CreationTime

Returns the <code>CreationTime</code> value.

#### FeatureGroupStatus

Returns the <code>FeatureGroupStatus</code> value.

#### FailureReason

Returns the <code>FailureReason</code> value.

#### OfflineStoreStatus

Returns the <code>OfflineStoreStatus</code> value.

