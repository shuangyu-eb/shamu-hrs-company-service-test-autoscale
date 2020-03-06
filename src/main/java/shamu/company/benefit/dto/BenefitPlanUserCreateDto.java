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
}
