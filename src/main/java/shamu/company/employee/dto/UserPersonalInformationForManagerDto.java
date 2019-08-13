package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.dto.BasicUserPersonalInformationDto;

@Data
public class UserPersonalInformationForManagerDto extends BasicUserPersonalInformationDto {

  private SelectFieldInformationDto gender;

  private SelectFieldInformationDto maritalStatus;

  private SelectFieldInformationDto ethnicity;

  private SelectFieldInformationDto citizenshipStatus;
}
