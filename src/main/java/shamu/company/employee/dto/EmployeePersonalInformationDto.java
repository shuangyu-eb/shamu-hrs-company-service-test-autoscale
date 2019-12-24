package shamu.company.employee.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import shamu.company.crypto.Crypto;
import shamu.company.crypto.CryptoSsnSerializer;
import shamu.company.user.entity.UserPersonalInformation;

@Data
public class EmployeePersonalInformationDto extends UserPersonalInformationForManagerDto {

  @Crypto(field = "id", targetType = UserPersonalInformation.class)
  @JsonSerialize(using = CryptoSsnSerializer.class)
  private String ssn;
}
