package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.user.entity.User;

@Data
public class BenefitPlanUserCreateDto {

  private String id;

  public BenefitPlanUser getBenefitPlanUser(final String benefitPlanId) {
    return new BenefitPlanUser(new User(this.id), new BenefitPlan(benefitPlanId), false);
  }
}
