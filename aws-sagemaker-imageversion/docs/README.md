# AWS::SageMaker::ImageVersion

Resource Type definition for AWS::SageMaker::ImageVersion

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::ImageVersion",
    "Properties" : {
        "<a href="#imagename" title="ImageName">ImageName</a>" : <i>String</i>,
        "<a href="#baseimage" title="BaseImage">BaseImage</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::ImageVersion
Properties:
    <a href="#imagename" title="ImageName">ImageName</a>: <i>String</i>
    <a href="#baseimage" title="BaseImage">BaseImage</a>: <i>String</i>
</pre>

## Properties

#### ImageName

The name of the image this version belongs to.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[A-Za-z0-9]([-.]?[A-Za-z0-9])*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### BaseImage

The registry path of the container image on which this image version is based.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Pattern_: <code>.+</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ImageVersionArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ImageVersionArn

The Amazon Resource Name (ARN) of the image version.

#### ImageArn

The Amazon Resource Name (ARN) of the parent image.

#### Version

The version number of the image version.

#### ContainerImage

The registry path of the container image that contains this image version.

