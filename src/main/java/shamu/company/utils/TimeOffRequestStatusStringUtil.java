package shamu.company.utils;

import shamu.company.timeoff.entity.TimeOffRequest;

public abstract class TimeOffRequestStatusStringUtil {

  private TimeOffRequestStatusStringUtil() {}

  public static String getUpperCaseString(final TimeOffRequest timeOffRequest) {

    final String status = timeOffRequest.getApprovalStatus().name();
    if (status.contains("_")) {
      final String[] strings = status.split("_");
      String result = upperCase(strings[0]);
      for (int i = 1; i < strings.length; i++) {
        result = result + " " + upperCase(strings[i]);
      }
      return result;
    }

    return upperCase(status);

  }

  public static String upperCase(final String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

}
