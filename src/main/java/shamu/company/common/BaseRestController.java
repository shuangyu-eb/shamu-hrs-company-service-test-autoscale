package shamu.company.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

public class BaseRestController {

  @Autowired private AuthUserCacheManager authUserCacheManager;

  @Autowired private UserService userService;

  protected DefaultJwtAuthenticationToken findAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (DefaultJwtAuthenticationToken) authentication;
  }

  protected String findToken() {
    return findAuthentication().getToken().getTokenValue();
  }

  public AuthUser findAuthUser() {
    final String token = findToken();
    if (authUserCacheManager.getCachedUser(token) == null) {
      throw new UnAuthenticatedException("User not logged in.");
    }

    return findAuthentication().getAuthUser();
  }

  public String findUserId() {
    return findAuthUser().getId();
  }

  public String findCompanyId() {
    return TenantContext.getCurrentTenant();
  }

  public boolean isCurrentUserSelfOrAdminOrManager(final String targetUserId) {
    final AuthUser currentUser = findAuthUser();
    final User targetUser = userService.findById(targetUserId);
    final User manager = targetUser.getManagerUser();
    final User.Role currentUserRole = currentUser.getRole();
    return currentUser.getId().equals(targetUserId)
        || currentUserRole == User.Role.ADMIN
        || currentUserRole == User.Role.SUPER_ADMIN
        || (manager != null && currentUser.getId().equals(manager.getId()));
  }
}
