package shamu.company.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TimeAndAttendanceDetailsDto {
  private static final String DATE_FORMAT = "MM/dd/yyyy";

  @JsonFormat(pattern = DATE_FORMAT)
  private Date payDate;

  private String payPeriodFrequency;

  @JsonFormat(pattern = DATE_FORMAT)
  private Date periodStartDate;

  @JsonFormat(pattern = DATE_FORMAT)
  private Date periodEndDate;

  private List<EmployeeOvertimeDetailsDto> overtimeDetails;
}
