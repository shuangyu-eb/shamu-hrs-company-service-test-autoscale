package shamu.company.benefit.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanTypeDto {
  private String benefitPlanTypeId;

  private String benefitPlanType;

  private Date benefitPlanEndDate;

  private String benefitPlanId;
}
