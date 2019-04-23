package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.entity.UserPersonalInformation;

@Data
public class UserPersonalInformationDto extends UserPersonalInformationForManagerDto {

  private String ssn;

  public UserPersonalInformationDto(UserPersonalInformation personalInformation) {
    super(personalInformation);
    this.ssn = personalInformation.getSsn();
  }
}
