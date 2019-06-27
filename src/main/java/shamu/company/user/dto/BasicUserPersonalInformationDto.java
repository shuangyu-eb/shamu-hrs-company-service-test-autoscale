package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class BasicUserPersonalInformationDto {

  @HashidsFormat
  private Long id;

  private String firstName;

  private String middleName;

  private String lastName;

  private String preferredName;

  private String birthDate;

  @HashidsFormat
  private Long maritalStatusId;

  private String maritalStatusName;

  public BasicUserPersonalInformationDto(UserPersonalInformation personalInformation) {
    this.id = personalInformation.getId();
    this.firstName = personalInformation.getFirstName();
    this.lastName = personalInformation.getLastName();
    this.middleName = personalInformation.getMiddleName();
    this.preferredName = personalInformation.getPreferredName();
    this.birthDate = personalInformation.getBirthDate() == null
        ? null : personalInformation.getBirthDate().toString();

    MaritalStatus maritalStatus = personalInformation.getMaritalStatus();
    Long maritalStateId = maritalStatus == null ? null : maritalStatus.getId();
    String maritalStateName = maritalStatus == null ? "" : maritalStatus.getName();
    this.maritalStatusId = maritalStateId;
    this.maritalStatusName = maritalStateName;
  }
}
