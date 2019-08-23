package shamu.company.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.info.dto.UserEmergencyContactDto;

@Data
@AllArgsConstructor
public class AccountInfoDto {

  private UserPersonalInformationDto userPersonalInformationDto;

  private String headPortrait;

  private UserAddressDto userAddress;

  private UserContactInformationDto userContactInformationDto;

  private List<UserEmergencyContactDto> userEmergencyContacts;
}
