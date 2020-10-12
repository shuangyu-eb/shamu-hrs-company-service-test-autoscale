package shamu.company.attendance.entity;

import java.sql.Timestamp;

public interface TimesheetPeriodPojo {
  String getId();

  Timestamp getStartDate();

  Timestamp getEndDate();
}
