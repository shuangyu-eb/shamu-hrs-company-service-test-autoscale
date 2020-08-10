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

  private String periodStartDate;

  private String periodEndDate;

  private String frontendTimezone;

  private List<EmployeeOvertimeDetailsDto> overtimeDetails;
}
