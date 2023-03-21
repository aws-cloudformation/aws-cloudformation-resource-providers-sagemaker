# AWS::SageMaker::ModelPackage

Resource Type definition for AWS::SageMaker::ModelPackage

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::SageMaker::ModelPackage",
    "Properties" : {
        "<a href="#tag" title="Tag">Tag</a>" : <i><a href="tag.md">Tag</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#additionalinferencespecifications" title="AdditionalInferenceSpecifications">AdditionalInferenceSpecifications</a>" : <i>[ <a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a>, ... ]</i>,
        "<a href="#additionalinferencespecificationdefinition" title="AdditionalInferenceSpecificationDefinition">AdditionalInferenceSpecificationDefinition</a>" : <i><a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a></i>,
        "<a href="#certifyformarketplace" title="CertifyForMarketplace">CertifyForMarketplace</a>" : <i>Boolean</i>,
        "<a href="#clienttoken" title="ClientToken">ClientToken</a>" : <i>String</i>,
        "<a href="#customermetadataproperties" title="CustomerMetadataProperties">CustomerMetadataProperties</a>" : <i><a href="customermetadataproperties.md">CustomerMetadataProperties</a></i>,
        "<a href="#domain" title="Domain">Domain</a>" : <i>String</i>,
        "<a href="#driftcheckbaselines" title="DriftCheckBaselines">DriftCheckBaselines</a>" : <i><a href="driftcheckbaselines.md">DriftCheckBaselines</a></i>,
        "<a href="#inferencespecification" title="InferenceSpecification">InferenceSpecification</a>" : <i><a href="inferencespecification.md">InferenceSpecification</a></i>,
        "<a href="#metadataproperties" title="MetadataProperties">MetadataProperties</a>" : <i><a href="metadataproperties.md">MetadataProperties</a></i>,
        "<a href="#modelapprovalstatus" title="ModelApprovalStatus">ModelApprovalStatus</a>" : <i>String</i>,
        "<a href="#modelmetrics" title="ModelMetrics">ModelMetrics</a>" : <i><a href="modelmetrics.md">ModelMetrics</a></i>,
        "<a href="#modelpackagedescription" title="ModelPackageDescription">ModelPackageDescription</a>" : <i>String</i>,
        "<a href="#modelpackagegroupname" title="ModelPackageGroupName">ModelPackageGroupName</a>" : <i>String</i>,
        "<a href="#modelpackagename" title="ModelPackageName">ModelPackageName</a>" : <i>String</i>,
        "<a href="#samplepayloadurl" title="SamplePayloadUrl">SamplePayloadUrl</a>" : <i>String</i>,
        "<a href="#sourcealgorithmspecification" title="SourceAlgorithmSpecification">SourceAlgorithmSpecification</a>" : <i><a href="sourcealgorithmspecification.md">SourceAlgorithmSpecification</a></i>,
        "<a href="#task" title="Task">Task</a>" : <i>String</i>,
        "<a href="#validationspecification" title="ValidationSpecification">ValidationSpecification</a>" : <i><a href="validationspecification.md">ValidationSpecification</a></i>,
        "<a href="#approvaldescription" title="ApprovalDescription">ApprovalDescription</a>" : <i>String</i>,
        "<a href="#lastmodifiedby" title="LastModifiedBy">LastModifiedBy</a>" : <i><a href="usercontext.md">UserContext</a></i>,
        "<a href="#lastmodifiedtime" title="LastModifiedTime">LastModifiedTime</a>" : <i>String</i>,
        "<a href="#modelpackageversion" title="ModelPackageVersion">ModelPackageVersion</a>" : <i>Integer</i>,
        "<a href="#additionalinferencespecificationstoadd" title="AdditionalInferenceSpecificationsToAdd">AdditionalInferenceSpecificationsToAdd</a>" : <i>[ <a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a>, ... ]</i>,
        "<a href="#modelpackagestatusdetails" title="ModelPackageStatusDetails">ModelPackageStatusDetails</a>" : <i><a href="modelpackagestatusdetails.md">ModelPackageStatusDetails</a></i>,
        "<a href="#modelpackagestatusitem" title="ModelPackageStatusItem">ModelPackageStatusItem</a>" : <i><a href="modelpackagestatusitem.md">ModelPackageStatusItem</a></i>,
        "<a href="#createdby" title="CreatedBy">CreatedBy</a>" : <i><a href="usercontext.md">UserContext</a></i>,
        "<a href="#environment" title="Environment">Environment</a>" : <i><a href="modelpackagecontainerdefinition-environment.md">Environment</a></i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::SageMaker::ModelPackage
Properties:
    <a href="#tag" title="Tag">Tag</a>: <i><a href="tag.md">Tag</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#additionalinferencespecifications" title="AdditionalInferenceSpecifications">AdditionalInferenceSpecifications</a>: <i>
      - <a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a></i>
    <a href="#additionalinferencespecificationdefinition" title="AdditionalInferenceSpecificationDefinition">AdditionalInferenceSpecificationDefinition</a>: <i><a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a></i>
    <a href="#certifyformarketplace" title="CertifyForMarketplace">CertifyForMarketplace</a>: <i>Boolean</i>
    <a href="#clienttoken" title="ClientToken">ClientToken</a>: <i>String</i>
    <a href="#customermetadataproperties" title="CustomerMetadataProperties">CustomerMetadataProperties</a>: <i><a href="customermetadataproperties.md">CustomerMetadataProperties</a></i>
    <a href="#domain" title="Domain">Domain</a>: <i>String</i>
    <a href="#driftcheckbaselines" title="DriftCheckBaselines">DriftCheckBaselines</a>: <i><a href="driftcheckbaselines.md">DriftCheckBaselines</a></i>
    <a href="#inferencespecification" title="InferenceSpecification">InferenceSpecification</a>: <i><a href="inferencespecification.md">InferenceSpecification</a></i>
    <a href="#metadataproperties" title="MetadataProperties">MetadataProperties</a>: <i><a href="metadataproperties.md">MetadataProperties</a></i>
    <a href="#modelapprovalstatus" title="ModelApprovalStatus">ModelApprovalStatus</a>: <i>String</i>
    <a href="#modelmetrics" title="ModelMetrics">ModelMetrics</a>: <i><a href="modelmetrics.md">ModelMetrics</a></i>
    <a href="#modelpackagedescription" title="ModelPackageDescription">ModelPackageDescription</a>: <i>String</i>
    <a href="#modelpackagegroupname" title="ModelPackageGroupName">ModelPackageGroupName</a>: <i>String</i>
    <a href="#modelpackagename" title="ModelPackageName">ModelPackageName</a>: <i>String</i>
    <a href="#samplepayloadurl" title="SamplePayloadUrl">SamplePayloadUrl</a>: <i>String</i>
    <a href="#sourcealgorithmspecification" title="SourceAlgorithmSpecification">SourceAlgorithmSpecification</a>: <i><a href="sourcealgorithmspecification.md">SourceAlgorithmSpecification</a></i>
    <a href="#task" title="Task">Task</a>: <i>String</i>
    <a href="#validationspecification" title="ValidationSpecification">ValidationSpecification</a>: <i><a href="validationspecification.md">ValidationSpecification</a></i>
    <a href="#approvaldescription" title="ApprovalDescription">ApprovalDescription</a>: <i>String</i>
    <a href="#lastmodifiedby" title="LastModifiedBy">LastModifiedBy</a>: <i><a href="usercontext.md">UserContext</a></i>
    <a href="#lastmodifiedtime" title="LastModifiedTime">LastModifiedTime</a>: <i>String</i>
    <a href="#modelpackageversion" title="ModelPackageVersion">ModelPackageVersion</a>: <i>Integer</i>
    <a href="#additionalinferencespecificationstoadd" title="AdditionalInferenceSpecificationsToAdd">AdditionalInferenceSpecificationsToAdd</a>: <i>
      - <a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a></i>
    <a href="#modelpackagestatusdetails" title="ModelPackageStatusDetails">ModelPackageStatusDetails</a>: <i><a href="modelpackagestatusdetails.md">ModelPackageStatusDetails</a></i>
    <a href="#modelpackagestatusitem" title="ModelPackageStatusItem">ModelPackageStatusItem</a>: <i><a href="modelpackagestatusitem.md">ModelPackageStatusItem</a></i>
    <a href="#createdby" title="CreatedBy">CreatedBy</a>: <i><a href="usercontext.md">UserContext</a></i>
    <a href="#environment" title="Environment">Environment</a>: <i><a href="modelpackagecontainerdefinition-environment.md">Environment</a></i>
</pre>

## Properties

#### Tag

A key-value pair to associate with a resource.

_Required_: No

_Type_: <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AdditionalInferenceSpecifications

An array of additional Inference Specification objects.

_Required_: No

_Type_: List of <a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AdditionalInferenceSpecificationDefinition

_Required_: No

_Type_: <a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CertifyForMarketplace

Whether to certify the model package for listing on AWS Marketplace.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ClientToken

A unique token that guarantees that the call to this API is idempotent.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>36</code>

_Pattern_: <code>^[a-zA-Z0-9-]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### CustomerMetadataProperties

The metadata properties associated with the model package versions.

_Required_: No

_Type_: <a href="customermetadataproperties.md">CustomerMetadataProperties</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Domain

The machine learning domain of the model package you specified.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### DriftCheckBaselines

Represents the drift check baselines that can be used when the model monitor is set using the model package.

_Required_: No

_Type_: <a href="driftcheckbaselines.md">DriftCheckBaselines</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InferenceSpecification

Details about inference jobs that can be run with models based on this model package.

_Required_: No

_Type_: <a href="inferencespecification.md">InferenceSpecification</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### MetadataProperties

Metadata properties of the tracking entity, trial, or trial component.

_Required_: No

_Type_: <a href="metadataproperties.md">MetadataProperties</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelApprovalStatus

The approval status of the model package.

_Required_: No

_Type_: String

_Allowed Values_: <code>Approved</code> | <code>Rejected</code> | <code>PendingManualApproval</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelMetrics

A structure that contains model metrics reports.

_Required_: No

_Type_: <a href="modelmetrics.md">ModelMetrics</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelPackageDescription

The description of the model package.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>[\p{L}\p{M}\p{Z}\p{S}\p{N}\p{P}]*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelPackageGroupName

The name of the model package group.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>170</code>

_Pattern_: <code>(arn:aws[a-z\-]*:sagemaker:[a-z0-9\-]*:[0-9]{12}:[a-z\-]*\/)?([a-zA-Z0-9]([a-zA-Z0-9-]){0,62})(?<!-)$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ModelPackageName

The name or arn of the model package.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SamplePayloadUrl

The Amazon Simple Storage Service (Amazon S3) path where the sample payload are stored pointing to single gzip compressed tar archive.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^(https|s3)://([^/]+)/?(.*)$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SourceAlgorithmSpecification

Details about the algorithm that was used to create the model package.

_Required_: No

_Type_: <a href="sourcealgorithmspecification.md">SourceAlgorithmSpecification</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Task

The machine learning task your model package accomplishes.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ValidationSpecification

Specifies configurations for one or more transform jobs that Amazon SageMaker runs to test the model package.

_Required_: No

_Type_: <a href="validationspecification.md">ValidationSpecification</a>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ApprovalDescription

A description provided for the model approval.

_Required_: No

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastModifiedBy

Information about the user who created or modified an experiment, trial, trial component, lineage group, or project.

_Required_: No

_Type_: <a href="usercontext.md">UserContext</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastModifiedTime

The time at which the model package was last modified.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageVersion

The version of the model package.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AdditionalInferenceSpecificationsToAdd

An array of additional Inference Specification objects.

_Required_: No

_Type_: List of <a href="additionalinferencespecificationdefinition.md">AdditionalInferenceSpecificationDefinition</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageStatusDetails

Details about the current status of the model package.

_Required_: No

_Type_: <a href="modelpackagestatusdetails.md">ModelPackageStatusDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ModelPackageStatusItem

_Required_: No

_Type_: <a href="modelpackagestatusitem.md">ModelPackageStatusItem</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CreatedBy

_Required_: No

_Type_: <a href="usercontext.md">UserContext</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Environment

_Required_: No

_Type_: <a href="modelpackagecontainerdefinition-environment.md">Environment</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ModelPackageArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ModelPackageArn

The Amazon Resource Name (ARN) of the model package group.

#### CreationTime

The time at which the model package was created.

#### ModelPackageStatus

The current status of the model package.

