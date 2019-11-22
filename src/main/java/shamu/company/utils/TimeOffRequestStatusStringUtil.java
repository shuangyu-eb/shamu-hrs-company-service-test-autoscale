package shamu.company.utils;

import shamu.company.timeoff.entity.TimeOffRequest;

public class TimeOffRequestStatusStringUtil {

  public static String getUpperCaseString(final TimeOffRequest timeOffRequest) {

    String status = timeOffRequest.getApprovalStatus().name();
    if (status.contains("_")) {
      String[] strings = status.split("_");
      String result = upperCase(strings[0]);
      for (int i = 1; i < strings.length; i++) {
        result = result + " " + upperCase(strings[i]);
      }
      return result;
    }

    return upperCase(status);

  }

  public static String upperCase(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

}
