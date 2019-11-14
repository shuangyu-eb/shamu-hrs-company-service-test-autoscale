package shamu.company.user.dto;

import lombok.Data;

@Data
public class MyEmployeePersonalInformationDto extends BasicUserPersonalInformationDto {

  private String genderId;

  private String genderName;

  private String ethnicityId;

  private String ethnicityName;

  private String imageUrl;

  private String maritalStatusId;

  private String maritalStatusName;
}
