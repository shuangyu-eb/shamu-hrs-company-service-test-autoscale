package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class BenefitPlanUserDto {

  @HashidsFormat
  private Long id;

  private String firstName;

  private String lastName;

  private String imageUrl;
}
