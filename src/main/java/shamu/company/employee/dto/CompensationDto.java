package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class CompensationDto {

  @HashidsFormat
  private Long id;

  private Integer wage;

  private SelectFieldInformationDto compensationFrequency;
}
