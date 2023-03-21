# AWS::SageMaker::ModelPackage TransformJobDefinition

Defines the input needed to run a transform job using the inference specification specified in the algorithm.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#environment" title="Environment">Environment</a>" : <i><a href="modelpackagecontainerdefinition-environment.md">Environment</a></i>,
    "<a href="#batchstrategy" title="BatchStrategy">BatchStrategy</a>" : <i>String</i>,
    "<a href="#maxconcurrenttransforms" title="MaxConcurrentTransforms">MaxConcurrentTransforms</a>" : <i>Integer</i>,
    "<a href="#maxpayloadinmb" title="MaxPayloadInMB">MaxPayloadInMB</a>" : <i>Integer</i>,
    "<a href="#transforminput" title="TransformInput">TransformInput</a>" : <i><a href="transforminput.md">TransformInput</a></i>,
    "<a href="#transformoutput" title="TransformOutput">TransformOutput</a>" : <i><a href="transformoutput.md">TransformOutput</a></i>,
    "<a href="#transformresources" title="TransformResources">TransformResources</a>" : <i><a href="transformresources.md">TransformResources</a></i>
}
</pre>

### YAML

<pre>
<a href="#environment" title="Environment">Environment</a>: <i><a href="modelpackagecontainerdefinition-environment.md">Environment</a></i>
<a href="#batchstrategy" title="BatchStrategy">BatchStrategy</a>: <i>String</i>
<a href="#maxconcurrenttransforms" title="MaxConcurrentTransforms">MaxConcurrentTransforms</a>: <i>Integer</i>
<a href="#maxpayloadinmb" title="MaxPayloadInMB">MaxPayloadInMB</a>: <i>Integer</i>
<a href="#transforminput" title="TransformInput">TransformInput</a>: <i><a href="transforminput.md">TransformInput</a></i>
<a href="#transformoutput" title="TransformOutput">TransformOutput</a>: <i><a href="transformoutput.md">TransformOutput</a></i>
<a href="#transformresources" title="TransformResources">TransformResources</a>: <i><a href="transformresources.md">TransformResources</a></i>
</pre>

## Properties

#### Environment

_Required_: No

_Type_: <a href="modelpackagecontainerdefinition-environment.md">Environment</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BatchStrategy

A string that determines the number of records included in a single mini-batch.

_Required_: No

_Type_: String

_Allowed Values_: <code>MultiRecord</code> | <code>SingleRecord</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaxConcurrentTransforms

The maximum number of parallel requests that can be sent to each instance in a transform job. The default value is 1.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MaxPayloadInMB

The maximum payload size allowed, in MB. A payload is the data portion of a record (without metadata).

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TransformInput

Describes the input source of a transform job and the way the transform job consumes it.

_Required_: Yes

_Type_: <a href="transforminput.md">TransformInput</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TransformOutput

Describes the results of a transform job.

_Required_: Yes

_Type_: <a href="transformoutput.md">TransformOutput</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TransformResources

Describes the resources, including ML instance types and ML instance count, to use for transform job.

_Required_: Yes

_Type_: <a href="transformresources.md">TransformResources</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

