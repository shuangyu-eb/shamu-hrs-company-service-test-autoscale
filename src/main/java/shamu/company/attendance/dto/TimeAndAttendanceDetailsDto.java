package shamu.company.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TimeAndAttendanceDetailsDto {

    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date payDate;

    private String payPeriodFrequency;

    private List<EmployeeOvertimeDetailsDto> overtimeDetails;
}
