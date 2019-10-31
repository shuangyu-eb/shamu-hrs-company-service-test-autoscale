package shamu.company.user.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.s3.PreSinged;

@Data
public class UserPersonalInformationDto extends BasicUserPersonalInformationDto {

  @HashidsFormat
  private Long genderId;

  private String genderName;

  @HashidsFormat
  private Long ethnicityId;

  private String ethnicityName;

  private String ssn;

  @PreSinged
  private String imageUrl;

  @HashidsFormat
  private Long maritalStatusId;

  private String maritalStatusName;
}
