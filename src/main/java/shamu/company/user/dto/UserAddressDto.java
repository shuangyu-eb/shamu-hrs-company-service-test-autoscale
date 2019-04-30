package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;

@Data
@NoArgsConstructor
public class UserAddressDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  private String street1;

  private String street2;

  private String city;

  private String countryName;

  @HashidsFormat
  private Long countryId;

  private String stateProvinceName;

  @HashidsFormat
  private Long stateProvinceId;

  private String postalCode;

  public UserAddressDto(UserAddress userAddress) {
    Country country = userAddress.getCountry();
    String countryName = country == null ? null : country.getName();
    Long countryId = country == null ? 1 : country.getId();

    StateProvince stateProvince = userAddress.getStateProvince();
    String stateProvinceName = stateProvince == null ? null : stateProvince.getName();
    Long stateProvinceId = stateProvince == null ? null : stateProvince.getId();

    User user = userAddress.getUser();
    Long userId = user == null ? null : user.getId();

    this.setCountryName(countryName);
    this.setCountryId(countryId);
    this.setStateProvinceId(stateProvinceId);
    this.setStateProvinceName(stateProvinceName);
    this.setUserId(userId);
    this.setId(userAddress.getId());
    this.setStreet1(userAddress.getStreet1());
    this.setStreet2(userAddress.getStreet2());
    this.setCity(userAddress.getCity());
    this.setPostalCode(userAddress.getPostalCode());
  }

  @JSONField(serialize = false)
  public UserAddress getUserAddress() {
    UserAddress userAddress = new UserAddress();
    userAddress.setId(this.getId());
    userAddress.setPostalCode(this.getPostalCode());
    userAddress.setCity(this.getCity());
    userAddress.setStreet1(this.getStreet1());
    userAddress.setStreet2(this.getStreet2());
    userAddress.setUser(new User(this.getUserId()));

    if (this.getStateProvinceId() != null) {
      userAddress.setStateProvince(new StateProvince(this.getStateProvinceId()));
    }

    if (this.getCountryId() != null) {
      userAddress.setCountry(new Country(this.getCountryId()));
    }

    return userAddress;
  }
}
