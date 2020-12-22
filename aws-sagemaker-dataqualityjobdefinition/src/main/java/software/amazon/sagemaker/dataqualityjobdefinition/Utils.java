package software.amazon.sagemaker.dataqualityjobdefinition;


import org.apache.commons.lang3.StringUtils;

public class Utils {

  /**
   * Get resource name from ARN.
   *
   * Since some resources use the physical id as the full arn, we need
   * a way to go from that to the resource name; since we use just the name
   * for all our api calls.
   * @param resourceArn String representation of the Resource's ARN.
   * @param substring The substring to partition on, that is followed
   * by the resource name.
   * @return The name portion of the ARN. Specifically the part that
   * follows the first substring
   */
  public static String getResourceNameFromArn(final String resourceArn,
                                              final String substring) {
    return StringUtils.substringAfter(resourceArn, substring);
  }

}