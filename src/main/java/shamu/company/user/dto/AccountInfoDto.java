package shamu.company.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.s3.PreSinged;

@Data
@AllArgsConstructor
public class AccountInfoDto {

  private UserPersonalInformationDto userPersonalInformationDto;

  @PreSinged
  private String headPortrait;

  private UserAddressDto userAddress;

  private UserContactInformationDto userContactInformationDto;

  private List<UserEmergencyContactDto> userEmergencyContacts;
}
