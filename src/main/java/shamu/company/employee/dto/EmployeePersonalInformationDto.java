package shamu.company.employee.dto;

import lombok.Data;

@Data
public class EmployeePersonalInformationDto extends UserPersonalInformationForManagerDto {

  private String ssn;
}
