# AWS::SageMaker::AppImageConfig FileSystemConfig

The Amazon Elastic File System (EFS) storage configuration for a SageMaker image.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#defaultgid" title="DefaultGid">DefaultGid</a>" : <i>Integer</i>,
    "<a href="#defaultuid" title="DefaultUid">DefaultUid</a>" : <i>Integer</i>,
    "<a href="#mountpath" title="MountPath">MountPath</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#defaultgid" title="DefaultGid">DefaultGid</a>: <i>Integer</i>
<a href="#defaultuid" title="DefaultUid">DefaultUid</a>: <i>Integer</i>
<a href="#mountpath" title="MountPath">MountPath</a>: <i>String</i>
</pre>

## Properties

#### DefaultGid

The default POSIX group ID (GID). If not specified, defaults to 100.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DefaultUid

The default POSIX user ID (UID). If not specified, defaults to 1000.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MountPath

The path within the image to mount the user's EFS home directory. The directory should be empty. If not specified, defaults to /home/sagemaker-user.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Pattern_: <code>^/.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

