package shamu.company.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanTypeWithoutExpiredDto {

  private String benefitPlanTypeId;

  private String benefitPlanType;

  private Number benefitPlanNumber = 0;
}
