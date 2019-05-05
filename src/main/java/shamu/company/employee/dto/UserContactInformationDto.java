package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.entity.UserContactInformation;

@Data
public class UserContactInformationDto extends BasicUserContactInformationDto {

  private String phoneHome;

  private String emailHome;

  public UserContactInformationDto(UserContactInformation contactInformation) {
    super(contactInformation);
    this.emailHome = contactInformation.getEmailHome();
    this.phoneHome = contactInformation.getPhoneHome();
  }
}
