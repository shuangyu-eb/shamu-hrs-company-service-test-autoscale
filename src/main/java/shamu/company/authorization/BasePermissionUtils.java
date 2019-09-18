package shamu.company.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.AuthUser;
import shamu.company.utils.Auth0Util;

class BasePermissionUtils {

  private AuthUserCacheManager authUserCacheManager;

  private Auth0Util auth0Util;

  @Autowired
  public void setParameters(
      AuthUserCacheManager authUserCacheManager,
      Auth0Util auth0Util) {
    this.authUserCacheManager = authUserCacheManager;
    this.auth0Util = auth0Util;
  }

  private DefaultJwtAuthenticationToken getAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (DefaultJwtAuthenticationToken) authentication;
  }

  private void checkIsAuthenticated() {
    String token = getAuthentication().getToken().getTokenValue();
    if (authUserCacheManager.getCachedUser(token) == null) {
      throw new UnAuthenticatedException("User not logged in.");
    }
  }

  protected AuthUser getAuthUser() {
    checkIsAuthenticated();
    String token = getAuthentication().getToken().getTokenValue();
    return authUserCacheManager.getCachedUser(token);
  }

  protected Long getCompanyId() {
    return getAuthUser().getCompanyId();
  }

  protected shamu.company.user.entity.User.Role getAuthUserRole() {
    return auth0Util.getUserRole(getAuthUser().getEmail());
  }
}
