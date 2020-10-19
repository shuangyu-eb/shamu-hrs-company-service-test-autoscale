package shamu.company.attendance.dto;

import lombok.Data;

import java.util.List;

@Data
public class TimeAndAttendanceDetailsDto {

  private String payDate;

  private String payPeriodFrequency;

  private String periodStartDate;

  private String periodEndDate;

  private Boolean isAddOrRemove;

  private List<String> removedUserIds;

  private List<EmployeeOvertimeDetailsDto> overtimeDetails;
}
