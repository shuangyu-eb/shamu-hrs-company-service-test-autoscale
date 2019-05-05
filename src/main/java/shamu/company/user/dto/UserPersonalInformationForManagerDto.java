package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class UserPersonalInformationForManagerDto extends BasicUserPersonalInformationDto {
  @HashidsFormat private Long genderId;

  private String genderName;

  @HashidsFormat private Long maritalStatusId;

  private String maritalStatusName;

  public UserPersonalInformationForManagerDto(UserPersonalInformation userPersonalInformation) {
    super(userPersonalInformation);
    Gender gender = userPersonalInformation.getGender();
    String genderName = gender == null ? "" : gender.getName();
    Long genderId = gender == null ? null : gender.getId();
    this.genderId = genderId;
    this.genderName = genderName;

    MaritalStatus maritalStatus = userPersonalInformation.getMaritalStatus();
    Long maritalStatusId = maritalStatus == null ? null : maritalStatus.getId();
    String maritalStatusName = maritalStatus == null ? "" : maritalStatus.getName();
    this.maritalStatusId = maritalStatusId;
    this.maritalStatusName = maritalStatusName;

    BeanUtils.copyProperties(userPersonalInformation, this);
  }
}
