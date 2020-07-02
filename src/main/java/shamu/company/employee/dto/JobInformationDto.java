package shamu.company.employee.dto;

import lombok.Data;

@Data
public class JobInformationDto extends BasicJobInformationDto {

  private CompensationDto compensation;

  private SelectFieldInformationDto employeeType;
}
