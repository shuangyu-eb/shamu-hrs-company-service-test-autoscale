package shamu.company.benefit.dto;

import java.util.List;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanDocument;

@Data
public class BenefitPlanUpdateDto {

  private BenefitPlanDto benefitPlan;

  private List<BenefitPlanCoverageDto> benefitPlanCoverages;

  private List<BenefitPlanUserDto> benefitPlanUsers;

  private RetirementPlanTypeDto retirementPlanType;

  private List<BenefitPlanDocument> benefitPlanDocuments;
}
