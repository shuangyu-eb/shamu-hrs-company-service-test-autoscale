package shamu.company.benefit.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanPreviewDto {
  private String benefitPlanId;

  private String benefitPlanName;

  private Integer eligibleNumber;

  private Integer enrolledNumber;

  private List<BenefitPlanUserDto> benefitPlanUsers;
}
