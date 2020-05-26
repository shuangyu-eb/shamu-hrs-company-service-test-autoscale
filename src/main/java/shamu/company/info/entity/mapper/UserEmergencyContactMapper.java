package shamu.company.info.entity.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.StringUtils;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.mapper.Config;
import shamu.company.info.dto.BasicUserEmergencyContactDto;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.user.entity.User;

@Mapper(config = Config.class)
public interface UserEmergencyContactMapper {

  @Mapping(target = "userId", source = "user.id")
  BasicUserEmergencyContactDto convertToBasicUserEmergencyContactDto(
      UserEmergencyContact userEmergencyContact);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "stateId", source = "state.id")
  @Mapping(target = "stateName", source = "state.name")
  @Mapping(target = "countryId", source = "country.id")
  @Mapping(target = "countryName", source = "country.name")
  UserEmergencyContactDto convertToUserEmergencyContactDto(
      UserEmergencyContact userEmergencyContact);

  @Mapping(target = "state", source = "stateId")
  @Mapping(target = "country", source = "countryId")
  @Mapping(target = "user", source = "userId")
  @Mapping(target = "id", source = "id")
  UserEmergencyContact createFromUserEmergencyContactDto(
      UserEmergencyContactDto userEmergencyContactDto);

  @InheritConfiguration(name = "createFromUserEmergencyContactDto")
  void updateFromUserEmergencyContactDto(
      @MappingTarget UserEmergencyContact emergencyContact,
      UserEmergencyContactDto userEmergencyContactDto);

  default StateProvince convertFromStateProvinceId(final String stateProvinceId) {
    return StringUtils.isEmpty(stateProvinceId) ? null : new StateProvince(stateProvinceId);
  }

  default User convertFromUserId(final String userId) {
    return !StringUtils.isEmpty(userId) ? new User(userId) : null;
  }

  default Country convertFromCountryId(final String countryId) {
    return StringUtils.isEmpty(countryId) ? null : new Country(countryId);
  }
}
