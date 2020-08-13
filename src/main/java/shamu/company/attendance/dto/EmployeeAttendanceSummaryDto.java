package shamu.company.attendance.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class EmployeeAttendanceSummaryDto {
  private String firstName;

  private String lastName;

  private Integer totalMinutes;

  private Integer regularMinutes;

  private Integer overtime15Minutes;

  private Integer overtime2Minutes;

  private Boolean approved;

  private Timestamp periodStartTime;

  private Timestamp periodEndTime;
}
