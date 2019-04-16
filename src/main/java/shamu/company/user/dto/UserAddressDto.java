package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressDto {

  private Long id;

  private Long userId;

  private String street1;

  private String street2;

  private String city;

  private String countryName;

  private String stateProvinceName;

  private Long stateProvinceId;

  private String postalCode;

  public UserAddressDto(UserAddress userAddress, Long userId) {
    String countryName = userAddress.getCountry().getName();
    Long stateProvinceId = userAddress.getStateProvince().getId();
    String stateProvinceName = userAddress.getStateProvince().getName();

    this.setCountryName(countryName);
    this.setStateProvinceId(stateProvinceId);
    this.setStateProvinceName(stateProvinceName);
    this.setUserId(userId);

    BeanUtils.copyProperties(userAddress, this);
  }

  public UserAddress getUserAddress(UserAddressDto userAddressDto, Country country) {
    UserAddress userAddress = new UserAddress();
    BeanUtils.copyProperties(userAddressDto,userAddress);
    userAddress.setUser(new User(userAddressDto.getUserId()));
    userAddress.setStateProvince(new StateProvince(userAddressDto.getStateProvinceId()));
    userAddress.setCountry(country);
    return userAddress;
  }
}
