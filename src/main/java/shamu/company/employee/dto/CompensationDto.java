package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.entity.UserCompensation;

@Data
public class CompensationDto {

  private Long id;

  private Integer wage;

  private SelectFieldInformationDto compensationFrequency;

  public CompensationDto(UserCompensation userCompensation) {
    this.id = userCompensation.getId();
    this.wage = userCompensation.getWage();
    this.compensationFrequency = new SelectFieldInformationDto(
        userCompensation.getCompensationFrequency());
  }
}
