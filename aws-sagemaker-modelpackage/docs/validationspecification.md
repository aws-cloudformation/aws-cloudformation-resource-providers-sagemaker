# AWS::SageMaker::ModelPackage ValidationSpecification

Specifies configurations for one or more transform jobs that Amazon SageMaker runs to test the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#validationprofiles" title="ValidationProfiles">ValidationProfiles</a>" : <i>[ <a href="validationprofile.md">ValidationProfile</a>, ... ]</i>,
    "<a href="#validationrole" title="ValidationRole">ValidationRole</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#validationprofiles" title="ValidationProfiles">ValidationProfiles</a>: <i>
      - <a href="validationprofile.md">ValidationProfile</a></i>
<a href="#validationrole" title="ValidationRole">ValidationRole</a>: <i>String</i>
</pre>

## Properties

#### ValidationProfiles

_Required_: Yes

_Type_: List of <a href="validationprofile.md">ValidationProfile</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ValidationRole

The IAM roles to be used for the validation of the model package.

_Required_: Yes

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>^arn:aws[a-z\-]*:iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

