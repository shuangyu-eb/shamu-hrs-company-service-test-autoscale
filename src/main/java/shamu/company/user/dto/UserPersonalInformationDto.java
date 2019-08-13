package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
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
}
