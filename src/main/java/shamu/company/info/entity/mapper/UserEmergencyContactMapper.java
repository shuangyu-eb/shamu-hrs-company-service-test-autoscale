package shamu.company.info.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
  UserEmergencyContactDto convertToUserEmergencyContactDto(
      UserEmergencyContact userEmergencyContact);

  @Mapping(target = "state", source = "stateId")
  @Mapping(target = "user", source = "userId")
  @Mapping(target = "id", source = "id")
  UserEmergencyContact createFromUserEmergencyContactDto(
      UserEmergencyContactDto userEmergencyContactDto);

  default StateProvince convertFromStateProvinceId(final Long stateProvinceId) {
    return new StateProvince(stateProvinceId);
  }

  default User convertFromUserId(final Long userId) {
    return userId != null ? new User(userId) : null;
  }
}
