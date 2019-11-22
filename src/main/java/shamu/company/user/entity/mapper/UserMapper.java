package shamu.company.user.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.admin.dto.MockUserDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.server.AuthUser;
import shamu.company.user.dto.UserAvatarDto;
import shamu.company.user.dto.UserDto;
import shamu.company.user.dto.UserRoleAndStatusInfoDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.utils.UserNameUtil;

@Mapper(config = Config.class, uses = UserNameUtil.class, imports = Role.class)
public interface UserMapper {

  @Mapping(target = "userAvatar", source = "imageUrl")
  @Mapping(target = "managerAvatar", source = "managerUser.imageUrl")
  UserAvatarDto convertToUserAvatarDto(User user);

  @Mapping(target = "userStatus", source = "userStatus.name")
  @Mapping(target = "deactivatedAt", source = "deactivatedAt")
  @Mapping(target = "userRole", source = "userRole.name")
  UserRoleAndStatusInfoDto convertToUserRoleAndStatusInfoDto(User user);

  @Mapping(target = "avatar", source = "imageUrl")
  @Mapping(target = "email", source = "userContactInformation.emailWork")
  @Mapping(target = "firstName", source = "userPersonalInformation.firstName")
  @Mapping(target = "preferredName", source = "userPersonalInformation.preferredName")
  @Mapping(target = "lastName", source = "userPersonalInformation.lastName")
  UserDto convertToUserDto(User user);

  List<UserDto> convertToUserDtos(List<User> users);

  @Mapping(target = "userRole", source = "user.userRole")
  BasicJobInformationDto convertToBasicJobInformationDto(User user);

  @Mapping(target = "firstName", source = "userPersonalInformation.firstName")
  @Mapping(target = "lastName", source = "userPersonalInformation.lastName")
  BenefitPlanUserDto convertToBenefitPlanUserDto(User user);


  @Mapping(target = "email", source = "userContactInformation.emailWork")
  @Mapping(target = "companyId", source = "company.id")
  @Mapping(target = "userId", source = "id")
  @Mapping(target = "role", source = "userRole")
  AuthUser convertToAuthUser(User user);

  @Mapping(target = "email", source = "userContactInformation.emailWork")
  @Mapping(target = "companyId", source = "company.id")
  MockUserDto convertToMockUserDto(User user);

  default Role convertFromUserRole(final UserRole userRole) {
    return Role.valueOf(userRole.getName());
  }
}
