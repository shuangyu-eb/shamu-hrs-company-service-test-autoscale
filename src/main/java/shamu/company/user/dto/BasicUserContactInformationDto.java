package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.validation.constraints.PhoneNumberValidate;
import shamu.company.user.entity.UserContactInformation;

@Data
@NoArgsConstructor
public class BasicUserContactInformationDto {

  private String id;

  private String userStatus;

  @PhoneNumberValidate
  private String phoneWork;

  @PhoneNumberValidate
  private String phoneHome;

  private String emailWork;

  public BasicUserContactInformationDto(UserContactInformation contactInformation) {
    this.id = contactInformation.getId();
    this.phoneWork = contactInformation.getPhoneWork();
    this.phoneHome = contactInformation.getPhoneHome();
    this.emailWork = contactInformation.getEmailWork();
  }
}
