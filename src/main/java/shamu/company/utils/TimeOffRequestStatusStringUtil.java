package shamu.company.utils;

import shamu.company.timeoff.entity.TimeOffRequest;

public interface TimeOffRequestStatusStringUtil {

  static String getUpperCaseString(final TimeOffRequest timeOffRequest) {

    final String status = timeOffRequest.getApprovalStatus().name();
    if (status.contains("_")) {
      final String[] strings = status.split("_");
      final StringBuilder result = new StringBuilder();
      result.append(upperCase(strings[0]));
      for (int i = 1; i < strings.length; i++) {
        result.append(" ");
        result.append(upperCase(strings[i]));
      }
      return result.toString();
    }

    return upperCase(status);
  }

  static String upperCase(final String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }
}
