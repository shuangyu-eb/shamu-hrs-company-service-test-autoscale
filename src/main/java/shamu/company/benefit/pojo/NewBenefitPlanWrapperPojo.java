package shamu.company.benefit.pojo;

import java.util.List;
import lombok.Data;

@Data
public class NewBenefitPlanWrapperPojo {

  private BenefitPlanPojo benefitPlan;

  private List<BenefitPlanCoveragePojo> coverages;

  private List<BenefitPlanUserPojo> selectedEmployees;
}
