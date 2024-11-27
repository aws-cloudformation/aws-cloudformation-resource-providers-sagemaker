# AWS::SageMaker::ModelPackage AdditionalInferenceSpecificationDefinition

Additional Inference Specification specifies details about inference jobs that can be run with models based on this model package.AdditionalInferenceSpecifications can be added to existing model packages using AdditionalInferenceSpecificationsToAdd.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#containers" title="Containers">Containers</a>" : <i>[ <a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a>, ... ]</i>,
    "<a href="#description" title="Description">Description</a>" : <i>String</i>,
    "<a href="#name" title="Name">Name</a>" : <i>String</i>,
    "<a href="#supportedcontenttypes" title="SupportedContentTypes">SupportedContentTypes</a>" : <i>[ String, ... ]</i>,
    "<a href="#supportedrealtimeinferenceinstancetypes" title="SupportedRealtimeInferenceInstanceTypes">SupportedRealtimeInferenceInstanceTypes</a>" : <i>[ String, ... ]</i>,
    "<a href="#supportedresponsemimetypes" title="SupportedResponseMIMETypes">SupportedResponseMIMETypes</a>" : <i>[ String, ... ]</i>,
    "<a href="#supportedtransforminstancetypes" title="SupportedTransformInstanceTypes">SupportedTransformInstanceTypes</a>" : <i>[ String, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#containers" title="Containers">Containers</a>: <i>
      - <a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a></i>
<a href="#description" title="Description">Description</a>: <i>String</i>
<a href="#name" title="Name">Name</a>: <i>String</i>
<a href="#supportedcontenttypes" title="SupportedContentTypes">SupportedContentTypes</a>: <i>
      - String</i>
<a href="#supportedrealtimeinferenceinstancetypes" title="SupportedRealtimeInferenceInstanceTypes">SupportedRealtimeInferenceInstanceTypes</a>: <i>
      - String</i>
<a href="#supportedresponsemimetypes" title="SupportedResponseMIMETypes">SupportedResponseMIMETypes</a>: <i>
      - String</i>
<a href="#supportedtransforminstancetypes" title="SupportedTransformInstanceTypes">SupportedTransformInstanceTypes</a>: <i>
      - String</i>
</pre>

## Properties

#### Containers

The Amazon ECR registry path of the Docker image that contains the inference code.

_Required_: Yes

_Type_: List of <a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

A description of the additional Inference specification.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

A unique name to identify the additional inference specification. The name must be unique within the list of your additional inference specifications for a particular model package.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>63</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9]){0,62}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedContentTypes

The supported MIME types for the input data.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedRealtimeInferenceInstanceTypes

A list of the instance types that are used to generate inferences in real-time

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedResponseMIMETypes

The supported MIME types for the output data.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedTransformInstanceTypes

A list of the instance types on which a transformation job can be run or on which an endpoint can be deployed.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

