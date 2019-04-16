package shamu.company.user.dto;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class UserPersonalInformationDto {

  private Long id;

  private String firstName;

  private String middleName;

  private String lastName;

  private String preferredName;

  private Timestamp birthDate;

  private String ssn;

  private Long genderId;

  private String genderName;

  private Long maritalStatusId;

  private String maritalStatusName;

  public UserPersonalInformationDto(UserPersonalInformation userPersonalInformation) {
    Long genderId = userPersonalInformation.getGender().getId();
    String genderName = userPersonalInformation.getGender().getName();
    Long maritalStatusId = userPersonalInformation.getMaritalStatus().getId();
    String maritalStatusName = userPersonalInformation.getMaritalStatus().getName();

    this.setGenderId(genderId);
    this.setGenderName(genderName);
    this.setMaritalStatusId(maritalStatusId);
    this.setMaritalStatusName(maritalStatusName);

    BeanUtils.copyProperties(userPersonalInformation, this);
  }

  public UserPersonalInformation getUserPersonalInformation(
      UserPersonalInformationDto userPersonalInformationDto) {
    UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    BeanUtils.copyProperties(userPersonalInformationDto, userPersonalInformation);
    userPersonalInformation.setGender(new Gender(userPersonalInformationDto.getGenderId()));
    userPersonalInformation.setMaritalStatus(
        new MaritalStatus(userPersonalInformationDto.getMaritalStatusId()));
    return userPersonalInformation;
  }
}
