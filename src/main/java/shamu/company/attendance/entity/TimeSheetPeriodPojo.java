package shamu.company.attendance.entity;

import java.sql.Timestamp;

public interface TimeSheetPeriodPojo {
  String getId();

  Timestamp getStartDate();

  Timestamp getEndDate();
}
