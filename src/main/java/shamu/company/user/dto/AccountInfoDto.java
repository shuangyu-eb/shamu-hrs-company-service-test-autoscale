package shamu.company.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.user.entity.UserAddress;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoDto {

  private UserPersonalInformationDto userPersonalInformationDto;

  private String headPortrait;

  private UserAddress userAddress;

  private UserContactInformationDto userContactInformationDto;

  private List<UserEmergencyContact> userEmergencyContacts;
}