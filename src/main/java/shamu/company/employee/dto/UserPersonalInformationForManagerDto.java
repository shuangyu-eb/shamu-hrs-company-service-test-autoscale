package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.entity.CitizenshipStatus;
import shamu.company.user.entity.Ethnicity;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;

@Data
public class UserPersonalInformationForManagerDto extends BasicUserPersonalInformationDto {

  private Gender gender;

  private MaritalStatus maritalStatus;

  private Ethnicity ethnicity;

  private CitizenshipStatus citizenshipStatus;

  public UserPersonalInformationForManagerDto(UserPersonalInformation personalInformation) {
    super(personalInformation);
    this.citizenshipStatus = personalInformation.getCitizenshipStatus();
    this.ethnicity = personalInformation.getEthnicity();
    this.maritalStatus = personalInformation.getMaritalStatus();
    this.gender = personalInformation.getGender();
  }
}
