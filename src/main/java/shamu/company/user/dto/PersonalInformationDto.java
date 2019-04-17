package shamu.company.user.dto;

import lombok.AllArgsConstructor;
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
    UserPersonalInformationDto userPersonalInformationDto =
        new UserPersonalInformationDto(userPersonalInformation);
    UserContactInformationDto userContactInformationDto =
        new UserContactInformationDto(userContactInformation);
    UserAddressDto userAddressDto = new UserAddressDto(userAddress);

    this.setUserAddressDto(userAddressDto);
    this.setUserContactInformationDto(userContactInformationDto);
    this.setUserPersonalInformationDto(userPersonalInformationDto);
    this.setUserId(userId);
  }
}
