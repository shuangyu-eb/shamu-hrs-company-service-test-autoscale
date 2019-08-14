package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.hashids.HashidsFormat;

@Data
public class BenefitDependentDto extends DependentPersonDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long employeeId;

  private SelectFieldInformationDto gender;

  private SelectFieldInformationDto state;

  private SelectFieldInformationDto relationShip;
}
