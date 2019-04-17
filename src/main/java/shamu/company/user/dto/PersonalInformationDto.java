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
    UserPersonalInformationDto userPersonalInformationDto =
        new UserPersonalInformationDto(userPersonalInformation);
    UserContactInformationDto userContactInformationDto =
        new UserContactInformationDto(userContactInformation);
    UserAddressDto userAddressDto = new UserAddressDto(userAddress, userId);

    this.setUserAddress(userAddressDto);
    this.setUserContactInformation(userContactInformationDto);
    this.setUserPersonalInformation(userPersonalInformationDto);
    this.setUserId(userId);
  }
}
