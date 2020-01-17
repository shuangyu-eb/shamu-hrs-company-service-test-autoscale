package shamu.company.benefit.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanCoverage;

@Data
public class  UserBenefitPlanDto {

  private String id;

  private String benefitPlanId;

  private String title;

  private String type;

  private String coverageType;

  private String coverageId;

  private Integer cost;

  private Integer employerCost;

  private Boolean enrolled = null;


  private List<BenefitPlanUserDto> dependents;

  private List<BenefitPlanCoverage> coverageOptions;

  private Timestamp benefitPlanStartDate;

  private Timestamp benefitPlanEndDate;
}
