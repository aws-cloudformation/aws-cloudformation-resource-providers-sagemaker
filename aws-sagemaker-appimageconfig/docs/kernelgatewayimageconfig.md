# AWS::SageMaker::AppImageConfig KernelGatewayImageConfig

The configuration for the file system and kernels in a SageMaker image running as a KernelGateway app.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#filesystemconfig" title="FileSystemConfig">FileSystemConfig</a>" : <i><a href="filesystemconfig.md">FileSystemConfig</a></i>,
    "<a href="#kernelspecs" title="KernelSpecs">KernelSpecs</a>" : <i>[ <a href="kernelspec.md">KernelSpec</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#filesystemconfig" title="FileSystemConfig">FileSystemConfig</a>: <i><a href="filesystemconfig.md">FileSystemConfig</a></i>
<a href="#kernelspecs" title="KernelSpecs">KernelSpecs</a>: <i>
      - <a href="kernelspec.md">KernelSpec</a></i>
</pre>

## Properties

#### FileSystemConfig

The Amazon Elastic File System (EFS) storage configuration for a SageMaker image.

_Required_: No

_Type_: <a href="filesystemconfig.md">FileSystemConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KernelSpecs

The specification of the Jupyter kernels in the image.

_Required_: Yes

_Type_: List of <a href="kernelspec.md">KernelSpec</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

