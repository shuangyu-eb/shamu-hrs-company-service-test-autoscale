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
  UserEmergencyContactDto convertToUserEmergencyContactDto(
      UserEmergencyContact userEmergencyContact);

  @Mapping(target = "state", source = "stateId")
  @Mapping(target = "user", source = "userId")
  UserEmergencyContact createFromUserEmergencyContactDto(
      UserEmergencyContactDto userEmergencyContactDto);

  default StateProvince convertToState(final Long id) {
    return new StateProvince(id);
  }

  default User convertToUser(final Long id) {
    return new User(id);
  }
}
