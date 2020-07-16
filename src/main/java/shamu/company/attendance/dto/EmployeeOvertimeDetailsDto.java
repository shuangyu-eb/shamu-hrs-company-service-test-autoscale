package shamu.company.attendance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class EmployeeOvertimeDetailsDto {
    private String employeeId;

    private String compensationUnit;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date hireDate;

    private String overtimeLaw;

    private Double regularPay;
}
