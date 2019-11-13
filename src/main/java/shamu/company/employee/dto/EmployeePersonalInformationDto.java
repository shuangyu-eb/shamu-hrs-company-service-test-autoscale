package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.crypto.Crypto;
import shamu.company.user.entity.UserPersonalInformation;

@Data
public class EmployeePersonalInformationDto extends UserPersonalInformationForManagerDto {

  @Crypto(field = "id", targetType = UserPersonalInformation.class)
  private String ssn;
}
