package shamu.company.timeoff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffPolicyRelatedUserDto {

  private String firstName;

  @HashidsFormat
  private Long id;

  private String imageUrl;

  private String jobTitle;

  private String lastName;
}
