# AWS::SageMaker::ModelPackage InferenceSpecification

Details about inference jobs that can be run with models based on this model package.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#containers" title="Containers">Containers</a>" : <i>[ <a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a>, ... ]</i>,
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

_Required_: Yes

_Type_: List of <a href="modelpackagecontainerdefinition.md">ModelPackageContainerDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedContentTypes

The supported MIME types for the input data.

_Required_: Yes

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedRealtimeInferenceInstanceTypes

A list of the instance types that are used to generate inferences in real-time

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedResponseMIMETypes

The supported MIME types for the output data.

_Required_: Yes

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SupportedTransformInstanceTypes

A list of the instance types on which a transformation job can be run or on which an endpoint can be deployed.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

