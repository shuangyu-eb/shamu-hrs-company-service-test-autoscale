package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.UserContactInformation;

@Data
@NoArgsConstructor
public class BasicUserContactInformationDto {

  @HashidsFormat
  private Long id;

  private String phoneWork;

  private String phoneHome;

  private String emailWork;

  public BasicUserContactInformationDto(UserContactInformation contactInformation) {
    this.id = contactInformation.getId();
    this.phoneWork = contactInformation.getPhoneWork();
    this.phoneHome = contactInformation.getPhoneHome();
    this.emailWork = contactInformation.getEmailWork();
  }
}
