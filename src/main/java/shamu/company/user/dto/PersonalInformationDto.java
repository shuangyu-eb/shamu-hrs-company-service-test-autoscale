package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class PersonalInformationDto {

  private Long userId;

  private UserAddressDto userAddressDto;

  private UserContactInformationDto userContactInformationDto;

  private UserPersonalInformationDto userPersonalInformationDto;

  public PersonalInformationDto(
      Long userId,
      UserPersonalInformation userPersonalInformation,
      UserContactInformation userContactInformation,
      UserAddress userAddress) {
    UserPersonalInformationDto userPersonalInfoHandled =
        new UserPersonalInformationDto(userPersonalInformation);
    UserContactInformationDto userContactInfoHandled =
        new UserContactInformationDto(userContactInformation);
    UserAddressDto userAddressHandled = new UserAddressDto(userAddress);
    userAddressHandled.setUserId(userId);

    this.setUserPersonalInformationDto(userPersonalInfoHandled);
    this.setUserContactInformationDto(userContactInfoHandled);
    this.setUserAddressDto(userAddressHandled);
    this.setUserId(userId);
  }
}
