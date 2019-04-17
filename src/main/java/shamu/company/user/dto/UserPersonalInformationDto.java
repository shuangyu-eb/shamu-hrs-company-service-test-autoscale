package shamu.company.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    Gender gender = userPersonalInformation.getGender();
    String genderName = gender == null ? "" : gender.getName();
    Long genderId = gender == null ? null : gender.getId();

    MaritalStatus maritalStatus = userPersonalInformation.getMaritalStatus();
    Long maritalStatusId = maritalStatus == null ? null : maritalStatus.getId();
    String maritalStatusName = maritalStatus == null ? null : maritalStatus.getName();

    this.setGenderId(genderId);
    this.setGenderName(genderName);
    this.setMaritalStatusId(maritalStatusId);
    this.setMaritalStatusName(maritalStatusName);

    BeanUtils.copyProperties(userPersonalInformation, this);
  }

  @JsonIgnore
  public UserPersonalInformation getUserPersonalInformation() {
    UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    BeanUtils.copyProperties(this, userPersonalInformation);
    userPersonalInformation.setGender(new Gender(this.getGenderId()));
    userPersonalInformation.setMaritalStatus(new MaritalStatus(this.getMaritalStatusId()));
    return userPersonalInformation;
  }
}
