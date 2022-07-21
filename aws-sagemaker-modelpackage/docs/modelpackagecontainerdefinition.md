# AWS::SageMaker::ModelPackage ModelPackageContainerDefinition

Describes the Docker container for the model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#containerhostname" title="ContainerHostname">ContainerHostname</a>" : <i>String</i>,
    "<a href="#environment" title="Environment">Environment</a>" : <i><a href="modelpackagecontainerdefinition-environment.md">Environment</a></i>,
    "<a href="#modelinput" title="ModelInput">ModelInput</a>" : <i><a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a></i>,
    "<a href="#image" title="Image">Image</a>" : <i>String</i>,
    "<a href="#imagedigest" title="ImageDigest">ImageDigest</a>" : <i>String</i>,
    "<a href="#modeldataurl" title="ModelDataUrl">ModelDataUrl</a>" : <i>String</i>,
    "<a href="#productid" title="ProductId">ProductId</a>" : <i>String</i>,
    "<a href="#framework" title="Framework">Framework</a>" : <i>String</i>,
    "<a href="#frameworkversion" title="FrameworkVersion">FrameworkVersion</a>" : <i>String</i>,
    "<a href="#nearestmodelname" title="NearestModelName">NearestModelName</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#containerhostname" title="ContainerHostname">ContainerHostname</a>: <i>String</i>
<a href="#environment" title="Environment">Environment</a>: <i><a href="modelpackagecontainerdefinition-environment.md">Environment</a></i>
<a href="#modelinput" title="ModelInput">ModelInput</a>: <i><a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a></i>
<a href="#image" title="Image">Image</a>: <i>String</i>
<a href="#imagedigest" title="ImageDigest">ImageDigest</a>: <i>String</i>
<a href="#modeldataurl" title="ModelDataUrl">ModelDataUrl</a>: <i>String</i>
<a href="#productid" title="ProductId">ProductId</a>: <i>String</i>
<a href="#framework" title="Framework">Framework</a>: <i>String</i>
<a href="#frameworkversion" title="FrameworkVersion">FrameworkVersion</a>: <i>String</i>
<a href="#nearestmodelname" title="NearestModelName">NearestModelName</a>: <i>String</i>
</pre>

## Properties

#### ContainerHostname

The DNS host name for the Docker container.

_Required_: No

_Type_: String

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Environment

Sets the environment variables in the Docker container

_Required_: No

_Type_: <a href="modelpackagecontainerdefinition-environment.md">Environment</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelInput

_Required_: No

_Type_: <a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Image

The Amazon EC2 Container Registry (Amazon ECR) path where inference code is stored.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Pattern_: <code>[\S]{1,255}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ImageDigest

An MD5 hash of the training algorithm that identifies the Docker image used for training.

_Required_: No

_Type_: String

_Maximum_: <code>72</code>

_Pattern_: <code>^[Ss][Hh][Aa]256:[0-9a-fA-F]{64}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelDataUrl

A structure with Model Input details.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProductId

The AWS Marketplace product ID of the model package.

_Required_: No

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Framework

The machine learning framework of the model package container image.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FrameworkVersion

The framework version of the Model Package Container Image.

_Required_: No

_Type_: String

_Minimum_: <code>3</code>

_Maximum_: <code>10</code>

_Pattern_: <code>[0-9]\.[A-Za-z0-9.]+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NearestModelName

The name of a pre-trained machine learning benchmarked by Amazon SageMaker Inference Recommender model that matches your model.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

