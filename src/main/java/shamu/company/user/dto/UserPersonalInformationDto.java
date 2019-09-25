package shamu.company.user.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class UserPersonalInformationDto extends BasicUserPersonalInformationDto {

  @HashidsFormat
  private Long genderId;

  private String genderName;

  @HashidsFormat
  private Long ethnicityId;

  private String ethnicityName;

  private String ssn;

  private String imageUrl;

  @HashidsFormat
  private Long maritalStatusId;

  private String maritalStatusName;
}
