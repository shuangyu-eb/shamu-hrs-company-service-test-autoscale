package shamu.company.attendance.utils.overtime;

import shamu.company.user.entity.UserCompensation;

public class OverTimePayFactory {
  OverTimePayFactory() {}

  private static final String NOT_ELIGIBLE = "Not Eligible";
  private static final String CALIFORNIA_POLICY = "California";
  private static final String ALASKA_POLICY = "Alaska";
  private static final String COLORADO_POLICY = "Colorado";
  private static final String KENTUCKY_POLICY = "Kentucky";
  private static final String NEVADA_POLICY = "Nevada";

  public static OverTimePay getOverTimePay(final UserCompensation userCompensation) {
    final String overTimeLaw = userCompensation.getOvertimeStatus().getName();
    switch (overTimeLaw) {
      case NOT_ELIGIBLE:
        return null;
      case CALIFORNIA_POLICY:
        return new CaliforniaOvertimePay();
      case ALASKA_POLICY:
        return new AlaskaOvertimePay();
      case COLORADO_POLICY:
        return new ColoradoOvertime();
      case KENTUCKY_POLICY:
        return new KentuckyOvertime();
      case NEVADA_POLICY:
      default:
        return new FederalOverTimePay();
    }
  }
}
