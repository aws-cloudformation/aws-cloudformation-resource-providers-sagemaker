# AWS::SageMaker::FeatureGroup FeatureDefinition

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#featurename" title="FeatureName">FeatureName</a>" : <i>String</i>,
    "<a href="#featuretype" title="FeatureType">FeatureType</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#featurename" title="FeatureName">FeatureName</a>: <i>String</i>
<a href="#featuretype" title="FeatureType">FeatureType</a>: <i>String</i>
</pre>

## Properties

#### FeatureName

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,63}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FeatureType

_Required_: Yes

_Type_: String

_Allowed Values_: <code>Integral</code> | <code>Fractional</code> | <code>String</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

