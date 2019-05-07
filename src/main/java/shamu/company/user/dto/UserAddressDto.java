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

  @HashidsFormat private Long id;

  @HashidsFormat private Long userId;

  private String street1;

  private String street2;

  private String city;

  private String countryName;

  @HashidsFormat private Long countryId;

  private String stateProvinceName;

  @HashidsFormat private Long stateProvinceId;

  private String postalCode;

  public UserAddressDto(UserAddress userAddress) {
    Country country = userAddress.getCountry();
    String countryName = country == null ? null : country.getName();
    Long countryId = country == null ? 1 : country.getId();
    this.countryName = countryName;
    this.countryId = countryId;

    StateProvince stateProvince = userAddress.getStateProvince();
    String stateProvinceName = stateProvince == null ? null : stateProvince.getName();
    Long stateProvinceId = stateProvince == null ? null : stateProvince.getId();
    this.stateProvinceId = stateProvinceId;
    this.stateProvinceName = stateProvinceName;

    User user = userAddress.getUser();
    Long userId = user == null ? null : user.getId();
    this.userId = userId;

    this.id = userAddress.getId();
    this.street1 = userAddress.getStreet1();
    this.street2 = userAddress.getStreet2();
    this.city = userAddress.getCity();
    this.postalCode = userAddress.getPostalCode();
  }

  @JSONField(serialize = false)
  public UserAddress getUserAddress(UserAddress origin) {
    origin.setId(this.getId());
    origin.setStreet1(this.getStreet1());
    origin.setStreet2(this.getStreet2());
    origin.setCity(this.getCity());
    origin.setPostalCode(this.getPostalCode());

    if (this.getStateProvinceId() != null) {
      origin.setStateProvince(new StateProvince(this.getStateProvinceId()));
    }

    if (this.getCountryId() != null) {
      origin.setCountry(new Country(this.getCountryId()));
    }

    return origin;
  }
}
