package shamu.company.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

public class TimeOffRequestStatusStringUtilTests {

  @Test
  void testGetUpperCaseString() {
    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        new TimeOffRequestApprovalStatus();

    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setTimeOffRequestApprovalStatus(timeOffRequestApprovalStatus);

    timeOffRequestApprovalStatus.setName(
        TimeOffRequestApprovalStatus.TimeOffApprovalStatus.NO_ACTION.name());
    Assertions.assertDoesNotThrow(
        () -> TimeOffRequestStatusStringUtil.getUpperCaseString(timeOffRequest));

    timeOffRequestApprovalStatus.setName(
        TimeOffRequestApprovalStatus.TimeOffApprovalStatus.DENIED.name());
    Assertions.assertDoesNotThrow(
        () -> TimeOffRequestStatusStringUtil.getUpperCaseString(timeOffRequest));
  }

  @Test
  void testUpperCase() {
    Assertions.assertDoesNotThrow(() -> TimeOffRequestStatusStringUtil.upperCase("abc"));
  }
}
