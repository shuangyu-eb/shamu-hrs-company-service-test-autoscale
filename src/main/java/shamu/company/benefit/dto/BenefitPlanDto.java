package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class BenefitPlanDto {

  @HashidsFormat
  private Long id;
}
