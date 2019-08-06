package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.entity.UserPersonalInformation;

@Data
public class UserPersonalInformationForManagerDto extends BasicUserPersonalInformationDto {

  private SelectFieldInformationDto gender;

  private SelectFieldInformationDto maritalStatus;

  private SelectFieldInformationDto ethnicity;

  private SelectFieldInformationDto citizenshipStatus;

  public UserPersonalInformationForManagerDto(final UserPersonalInformation personalInformation) {
    super(personalInformation);
    this.citizenshipStatus =
        new SelectFieldInformationDto(personalInformation.getCitizenshipStatus());
    this.ethnicity =
        new SelectFieldInformationDto(personalInformation.getEthnicity());
    this.maritalStatus = new SelectFieldInformationDto(personalInformation.getMaritalStatus());
    this.gender = new SelectFieldInformationDto(personalInformation.getGender());
  }
}
