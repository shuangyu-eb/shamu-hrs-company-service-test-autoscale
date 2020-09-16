package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.attendance.dto.OvertimePolicyDto;

@Data
public class CompensationDto {

  private String id;

  private Double wage;

  private OvertimePolicyDto overtimePolicy;

  private SelectFieldInformationDto compensationFrequency;
}
