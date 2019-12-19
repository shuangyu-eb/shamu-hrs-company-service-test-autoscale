package shamu.company.benefit.dto;

import java.util.List;
import lombok.Data;

@Data
public class NewBenefitPlanWrapperDto {

  private BenefitPlanCreateDto benefitPlan;

  private List<BenefitPlanCoverageDto> coverages;

  private List<BenefitPlanUserCreateDto> selectedEmployees;

  private Boolean forAllEmployees;
}
