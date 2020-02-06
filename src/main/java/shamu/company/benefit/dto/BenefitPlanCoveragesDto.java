package shamu.company.benefit.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BenefitPlanCoveragesDto {
  private List<BenefitCoveragesDto> coverages;
}
