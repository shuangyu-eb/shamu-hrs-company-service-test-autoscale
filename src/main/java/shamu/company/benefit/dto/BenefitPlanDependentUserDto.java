package shamu.company.benefit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenefitPlanDependentUserDto {
  String id;
  String firstName;
  String lastName;
}
