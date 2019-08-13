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
}
