package shamu.company.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.employee.dto.CompensationDto;

@Data
@AllArgsConstructor
public class PayrollSetupEmployeeDto {

  private String employeeId;

  private String imageUrl;

  private String name;

  private String startDate;

  private String employeeType;

  private CompensationDto compensation;
}
