package shamu.company.benefit.dto;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanPreviewDto {
  private String benefitPlanId;

  private String benefitPlanName;

  private Timestamp deductionsBegin;

  private Timestamp deductionsEnd;

  private String status;

  private Number eligibleNumber;

  private Number enrolledNumber;
}
