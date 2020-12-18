# AWS::SageMaker::Project

Resource Type definition for AWS::SageMaker::Project

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::Project",
    "Properties" : {
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#projectname" title="ProjectName">ProjectName</a>" : <i>String</i>,
        "<a href="#projectdescription" title="ProjectDescription">ProjectDescription</a>" : <i>String</i>,
        "<a href="#servicecatalogprovisioningdetails" title="ServiceCatalogProvisioningDetails">ServiceCatalogProvisioningDetails</a>" : <i><a href="servicecatalogprovisioningdetails.md">ServiceCatalogProvisioningDetails</a></i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::Project
Properties:
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#projectname" title="ProjectName">ProjectName</a>: <i>String</i>
    <a href="#projectdescription" title="ProjectDescription">ProjectDescription</a>: <i>String</i>
    <a href="#servicecatalogprovisioningdetails" title="ServiceCatalogProvisioningDetails">ServiceCatalogProvisioningDetails</a>: <i><a href="servicecatalogprovisioningdetails.md">ServiceCatalogProvisioningDetails</a></i>
</pre>

## Properties

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ProjectName

The name of the project.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>32</code>

_Pattern_: <code>^[a-zA-Z0-9](-*[a-zA-Z0-9])*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ProjectDescription

The description of the project.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>.*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ServiceCatalogProvisioningDetails

Input ServiceCatalog Provisioning Details

_Required_: Yes

_Type_: <a href="servicecatalogprovisioningdetails.md">ServiceCatalogProvisioningDetails</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ProjectArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ProjectArn

The Amazon Resource Name (ARN) of the Project.

#### CreationTime

The time at which the project was created.

#### ProjectId

Project Id.

#### ServiceCatalogProvisionedProductDetails

Provisioned ServiceCatalog  Details

#### ProjectStatus

The status of a project.

