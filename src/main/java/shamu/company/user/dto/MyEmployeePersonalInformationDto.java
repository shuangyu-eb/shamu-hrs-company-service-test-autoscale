package shamu.company.user.dto;

import lombok.Data;
import shamu.company.helpers.s3.PreSinged;

@Data
public class MyEmployeePersonalInformationDto extends BasicUserPersonalInformationDto {

  private String genderId;

  private String genderName;

  private String ethnicityId;

  private String ethnicityName;

  @PreSinged
  private String imageUrl;

  private String maritalStatusId;

  private String maritalStatusName;
}
