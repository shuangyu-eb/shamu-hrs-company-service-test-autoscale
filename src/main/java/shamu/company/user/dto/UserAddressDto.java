package shamu.company.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;

@Data
@NoArgsConstructor
public class UserAddressDto {

  private Long id;

  private Long userId;

  private String street1;

  private String street2;

  private String city;

  private String countryName;

  private Long countryId;

  private String stateProvinceName;

  private Long stateProvinceId;

  private String postalCode;

  public UserAddressDto(UserAddress source) {
    Country country = source.getCountry();
    String countryName = country == null ? "" : country.getName();
    Long countryId = country == null ? null : country.getId();

    StateProvince stateProvince = source.getStateProvince();
    String stateProvinceName = stateProvince == null ? "" : stateProvince.getName();
    Long stateProvinceId = stateProvince == null ? null : stateProvince.getId();

    Long userId = source.getUser().getId();

    this.setCountryName(countryName);
    this.setCountryId(countryId);
    this.setStateProvinceId(stateProvinceId);
    this.setStateProvinceName(stateProvinceName);
    this.setUserId(userId);
    this.setId(source.getId());
    this.setStreet1(source.getStreet1());
    this.setStreet2(source.getStreet2());
    this.setCity(source.getCity());
    this.setPostalCode(source.getPostalCode());
  }

  @JsonIgnore
  public UserAddress getUserAddress() {
    UserAddress userAddress = new UserAddress();
    userAddress.setId(this.getId());
    userAddress.setPostalCode(this.getPostalCode());
    userAddress.setCity(this.getCity());
    userAddress.setStreet1(this.getStreet1());
    userAddress.setStreet2(this.getStreet2());
    userAddress.setUser(new User(this.getUserId()));
    userAddress.setStateProvince(new StateProvince(this.getStateProvinceId()));
    userAddress.setCountry(new Country(this.getCountryId()));
    return userAddress;
  }
}
