package shamu.company.benefit.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanPreviewDto {
  @HashidsFormat
  private Long benefitPlanId;

  private String benefitPlanName;

  private Integer eligibleNumber;

  private Integer enrolledNumber;

  private List<BenefitPlanUserDto> benefitPlanUsers;
}
