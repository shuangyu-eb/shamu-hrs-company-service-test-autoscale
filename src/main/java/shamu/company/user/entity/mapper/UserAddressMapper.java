package shamu.company.user.entity.mapper;

import org.apache.commons.lang.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.mapper.Config;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserAddress;

@Mapper(config = Config.class)
public interface UserAddressMapper {

  @Mapping(target = "countryName", source = "country.name")
  @Mapping(target = "countryId", source = "country.id")
  @Mapping(target = "stateId", source = "stateProvince.id")
  @Mapping(target = "stateName", source = "stateProvince.name")
  @Mapping(target = "userId", source = "user.id")
  UserAddressDto convertToUserAddressDto(UserAddress userAddress);

  @Mapping(target = "stateProvince", source = "stateId")
  @Mapping(target = "country", source = "countryId")
  @Mapping(target = "user", source = "userId")
  void updateFromUserAddressDto(@MappingTarget UserAddress userAddress,
      UserAddressDto userAddressDto);

  @Mapping(target = "stateProvince", source = "stateId")
  @Mapping(target = "country", source = "countryId")
  @Mapping(target = "user", source = "userId")
  UserAddress createFromUserAddressDto(UserAddressDto userAddressDto);

  default StateProvince convertFromStateProvinceId(final String id) {
    return StringUtils.isEmpty(id)  ? null : new StateProvince(id);
  }

  default Country convertFromCountryId(final String id) {
    return StringUtils.isEmpty(id) ? null : new Country(id);
  }

  default User convertFromUserId(final String userId) {
    return StringUtils.isEmpty(userId) ? null : new User(userId);
  }
}

