package shamu.company.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.AuthUser;

class BasePermissionUtils {

  private AuthUserCacheManager authUserCacheManager;

  @Autowired
  public void setParameters(final AuthUserCacheManager authUserCacheManager) {
    this.authUserCacheManager = authUserCacheManager;
  }

  private DefaultJwtAuthenticationToken getAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (DefaultJwtAuthenticationToken) authentication;
  }

  private void checkIsAuthenticated() {
    final String token = getAuthentication().getToken().getTokenValue();
    if (authUserCacheManager.getCachedUser(token) == null) {
      throw new UnAuthenticatedException("User not logged in.");
    }
  }

  protected AuthUser getAuthUser() {
    checkIsAuthenticated();
    final String token = getAuthentication().getToken().getTokenValue();
    return authUserCacheManager.getCachedUser(token);
  }

  protected Long getCompanyId() {
    return getAuthUser().getCompanyId();
  }

  String getAuth0UserId() {
    checkIsAuthenticated();
    return getAuthentication().getUserId();
  }
}
