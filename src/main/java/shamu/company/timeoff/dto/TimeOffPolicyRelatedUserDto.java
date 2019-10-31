package shamu.company.timeoff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.s3.PreSinged;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffPolicyRelatedUserDto {

  private String firstName;

  @HashidsFormat
  private Long id;

  @PreSinged
  private String imageUrl;

  private String jobTitle;

  private String lastName;

  private Integer balance;
}
