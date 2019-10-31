package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.s3.PreSinged;

@Data
public class BenefitPlanUserDto {

  @HashidsFormat
  private Long id;

  private String firstName;

  private String lastName;

  @PreSinged
  private String imageUrl;
}
