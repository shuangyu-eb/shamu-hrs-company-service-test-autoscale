package shamu.company.authorization;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import shamu.company.authorization.Permission.PermissionType;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

@Component
public class PermissionUtils {

  private final UserService userService;

  private final UserAddressService userAddressService;

  @Autowired
  public PermissionUtils(UserService userService, UserAddressService userAddressService) {
    this.userService = userService;
    this.userAddressService = userAddressService;
  }

  boolean hasPermission(Authentication auth, Long targetId, Type targetType,
      Permission.Name permission) {

    switch (targetType) {
      case USER_PERSONAL_INFORMATION:
        return this.hasPermissionOfPersonalInformation(auth, targetId, permission);
      case USER_ADDRESS:
        return this.hasPermissionOfUserAddress(auth, targetId, permission);
      case USER_CONTACT_INFORMATION:
        return this.hasPermissionOfContactInformation(auth, targetId, permission);
      case USER:
      default:
        User targetUser = userService.findUserById(targetId);
        return this.hasPermissionOfUser(auth, targetUser, permission);
    }
  }

  private boolean hasPermission(Collection<AuthorityPojo> authorities,
      Permission.Name permission) {

    return authorities.stream()
        .anyMatch(authority -> authority.getName() == permission);
  }

  private void companyEqual(Authentication auth, Company company) {
    if (!this.getCompany(auth).getId().equals(company.getId())) {
      throw new ForbiddenException("The target resources is not in the company where you are.");
    }
  }

  private boolean hasPermissionOfPersonalInformation(
      Authentication auth, Long id, Permission.Name permission) {
    User user = userService.findUserByUserPersonalInformationId(id);

    return this.hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfUserAddress(
      Authentication auth, Long id, Permission.Name permission) {
    User user = userAddressService.findUserAddressById(id).getUser();
    return this.hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfContactInformation(
      Authentication auth, Long id, Permission.Name permission) {
    User user = userService.findUserByUserContactInformationId(id);
    return this.hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfUser(Authentication auth, User targetUser,
      Permission.Name permission) {
    PermissionType permissionType = permission.getPermissionType();
    if (permissionType == PermissionType.SELF_PERMISSION) {
      return this.getUser(auth).getId().equals(targetUser.getId());
    }

    this.companyEqual(auth, targetUser.getCompany());

    boolean result;
    Collection<AuthorityPojo> authorities = (Collection<AuthorityPojo>) auth.getAuthorities();
    if (permissionType != PermissionType.MANAGER_PERMISSION) {
      result = hasPermission(authorities, permission);
    } else {
      result = authorities.stream().anyMatch(p -> {
            Permission.Name permissionName = p.getName();
            if (permission == permissionName) {
              if (p.getIds() != null) {
                return p.getIds().contains(targetUser.getId());
              }
              return true;
            }
            return false;
          }
      );
    }

    return result;
  }

  private User getUser(Authentication auth) {
    return (User) auth.getPrincipal();
  }

  private Company getCompany(Authentication auth) {
    return this.getUser(auth).getCompany();
  }
}
