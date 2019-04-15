package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalInformationDto {
  private Long userId;

  private UserAddressDto userAddress;

  private UserContactInformationDto userContactInformation;

  private UserPersonalInformationDto userPersonalInformation;

  public PersonalInformationDto(
      Long userId,
      UserPersonalInformation userPersonalInformation,
      UserContactInformation userContactInformation,
      UserAddress userAddress) {
    UserPersonalInformationDto userPersonalInformationDtO =
        new UserPersonalInformationDto(userPersonalInformation);
    UserContactInformationDto userContactInformationDtO =
        new UserContactInformationDto(userContactInformation);
    UserAddressDto userAddressDtO = new UserAddressDto(userAddress, userId);

    this.setUserAddress(userAddressDtO);
    this.setUserContactInformation(userContactInformationDtO);
    this.setUserPersonalInformation(userPersonalInformationDtO);
    this.setUserId(userId);
  }
}
