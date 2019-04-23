package shamu.company.employee.dto;

import lombok.Data;
import shamu.company.user.entity.UserContactInformation;

@Data
public class BasicUserContactInformationDto {

  private String phoneWork;

  private String phoneWorkExtension;

  private String phoneMobile;

  private String emailWork;

  public BasicUserContactInformationDto(UserContactInformation contactInformation) {
    this.phoneMobile = contactInformation.getPhoneMobile();
    this.phoneWork = contactInformation.getPhoneWork();
    this.emailWork = contactInformation.getEmailWork();
    this.phoneWorkExtension = contactInformation.getPhoneWorkExtension();
  }
}
