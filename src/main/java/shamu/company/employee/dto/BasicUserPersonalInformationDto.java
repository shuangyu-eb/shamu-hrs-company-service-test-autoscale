package shamu.company.employee.dto;

import java.sql.Date;
import lombok.Data;
import shamu.company.user.entity.UserPersonalInformation;

@Data
public class BasicUserPersonalInformationDto {

  private String firstName;

  private String middleName;

  private String lastName;

  private String preferredName;

  private Date birthDate;

  public BasicUserPersonalInformationDto(UserPersonalInformation personalInformation) {
    this.firstName = personalInformation.getFirstName();
    this.lastName = personalInformation.getLastName();
    this.middleName = personalInformation.getMiddleName();
    this.preferredName = personalInformation.getPreferredName();
    this.birthDate = personalInformation.getBirthDate();
  }
}
