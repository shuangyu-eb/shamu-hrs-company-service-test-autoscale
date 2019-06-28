package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.common.entity.StateProvince;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.Gender;

@Data
public class BenefitDependentDto extends DependentPersonDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long employeeId;

  private SelectFieldInformationDto gender;

  private SelectFieldInformationDto state;

  private SelectFieldInformationDto relationShip;

  public BenefitDependentDto(BenefitPlanDependent benefitPlanDependent) {
    super(benefitPlanDependent);
    this.id = benefitPlanDependent.getId();
    this.employeeId = benefitPlanDependent.getEmployee().getId();
    setGender(benefitPlanDependent.getGender());
    setRelationShip(benefitPlanDependent.getDependentRelationship());
    setState(benefitPlanDependent.getState());
  }

  public void setGender(Gender gender) {
    if (gender != null) {
      this.gender = new SelectFieldInformationDto(gender);
    }
  }

  public void setState(StateProvince state) {
    if (state != null) {
      this.state = new SelectFieldInformationDto(state);
    }
  }

  public void setRelationShip(DependentRelationship relationShip) {
    if (relationShip != null) {
      this.relationShip = new SelectFieldInformationDto(relationShip);
    }
  }
}
