package shamu.company.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanUserCreateDto {

  private String id;

  private String coverage;

  private Double annualMaximum;

  private String companyContribution;

  private Double contributionValue;

  private Double deductionValue;

  private String employeeDeduction;

  private Boolean isDeductionLimit;
}
