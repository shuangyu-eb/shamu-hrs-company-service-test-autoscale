package shamu.company.employee.dto;

import lombok.Data;

@Data
public class CompensationDto {

  private String id;

  private Double wage;

  private CompensationOvertimeStatusDto overtimeStatus;

  private SelectFieldInformationDto compensationFrequency;
}
