package shamu.company.authorization;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import shamu.company.authorization.Permission.PermissionType;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@Component
public class PermissionUtils {

  @Autowired
  UserService userService;

  boolean hasAuthority(Permission.Name permission) {
    Collection<? extends GrantedAuthority> authorities = this.getAuthorities();

    return this.hasPermission(authorities, permission);
  }

  boolean hasAnyPermission(Long targetId, Type targetType, Permission.Name[] permissions) {
    if (permissions.length <= 0) {
      throw new ForbiddenException("Please select a Permission.");
    }

    Collection<? extends GrantedAuthority> authorities = this.getAuthorities();
    if (permissions.length == 1) {
      return this.hasPermission(authorities, targetId, targetType, permissions[0]);
    } else {
      return Stream.of(permissions).anyMatch(
          permission -> this.hasPermission(authorities, targetId, targetType, permission));
    }
  }

  boolean hasPermission(Long targetId, Type targetType, Permission.Name permission) {
    Collection<? extends GrantedAuthority> authorities = this.getAuthorities();

    return this.hasPermission(authorities, targetId, targetType, permission);
  }

  private boolean hasPermission(Collection<? extends GrantedAuthority> authorities,
      Long targetId, Type targetType, Permission.Name permission) {

    switch (targetType) {
      case PERSONAL_INFORMATION:
        return this.hasPermissionOfPersonalInformation(authorities, targetId, permission);
      case USER:
      default:
        User targetUser = userService.findUserById(targetId);
        return this.hasPermissionOfUser(authorities, targetUser, permission);
    }
  }

  private boolean hasPermission(Collection<? extends GrantedAuthority> authorities,
      Permission.Name permission) {

    return authorities.stream()
        .anyMatch(authentication -> authentication.getAuthority().equals(permission.name()));
  }

  private void companyEqual(Company company) {
    if (!this.getCompany().getId().equals(company.getId())) {
      throw new ForbiddenException("The target resources is not in the company where you are.");
    }
  }

  private boolean hasPermissionOfUser(Collection<? extends GrantedAuthority> authorities,
      User targetUser, Permission.Name permission) {
    PermissionType permissionType = permission.getPermissionType();

    if (permissionType == PermissionType.SELF_PERMISSION) {
      return this.getUser().getId().equals(targetUser.getId());
    }

    this.companyEqual(targetUser.getCompany());
    boolean result;
    if (permissionType != PermissionType.MANAGER_PERMISSION) {
      result = hasPermission(authorities, permission);
    } else {
      result = authorities.stream().anyMatch(auth -> {
            String authority = auth.getAuthority();
            String permissionName = permission.name();
            if (authority.split(":").length > 1) {
              String[] authAndId = authority.split(":");
              if (authAndId[0].equals(permissionName)) {
                List<Long> ids =
                    Stream.of(authAndId[1].split("[^0-9]+"))
                        .filter(id -> !id.isEmpty())
                        .map(Long::parseLong).collect(Collectors.toList());
                return ids.contains(targetUser.getId());
              }
              return false;
            } else {
              return authority.equals(permissionName);
            }
          }
      );
    }

    return result;
  }

  private boolean hasPermissionOfPersonalInformation(
      Collection<? extends GrantedAuthority> authorities, Long id, Permission.Name permission) {
    User user = userService.findUserByUserPersonalInformationId(id);

    return this.hasPermissionOfUser(authorities, user, permission);
  }

  private Authentication getAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      throw new UnAuthenticatedException("User not logged in.");
    }
    return authentication;
  }

  private Collection<? extends GrantedAuthority> getAuthorities() {
    return this.getAuthentication().getAuthorities();
  }

  private User getUser() {
    return (User) this.getAuthentication().getPrincipal();
  }

  public Company getCompany() {
    return this.getUser().getCompany();
  }
}
