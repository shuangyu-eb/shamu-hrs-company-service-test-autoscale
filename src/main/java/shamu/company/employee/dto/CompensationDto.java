package shamu.company.employee.dto;

import lombok.Data;

@Data
public class CompensationDto {

  private String id;

  private Integer wage;

  private SelectFieldInformationDto compensationFrequency;
}
