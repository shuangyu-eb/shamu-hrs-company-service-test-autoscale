package shamu.company.benefit.pojo;

import lombok.Data;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;

@Data
public class BenefitPlanUserPojo {

  @HashidsFormat
  private Long id;

  public BenefitPlanUser getBenefitPlanUser(Long benefitPlanId) {
    return new BenefitPlanUser(new User(this.id), new BenefitPlan(benefitPlanId), false);
  }
}
