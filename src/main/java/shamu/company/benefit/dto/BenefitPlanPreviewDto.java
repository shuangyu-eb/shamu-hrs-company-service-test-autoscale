package shamu.company.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanPreviewDto {
  private String benefitPlanId;

  private String benefitPlanName;

  private Number eligibleNumber;

  private Number enrolledNumber;
}
