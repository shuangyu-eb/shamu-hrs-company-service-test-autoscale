package shamu.company.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;

class BasePermissionUtils {

  private AuthUserCacheManager authUserCacheManager;

  @Autowired
  public void setParameters(final AuthUserCacheManager authUserCacheManager) {
    this.authUserCacheManager = authUserCacheManager;
  }

  private DefaultJwtAuthenticationToken getAuthentication() {

    final DefaultJwtAuthenticationToken authentication =
        (DefaultJwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || authUserCacheManager.getCachedUser(authentication.getToken().getTokenValue()) == null) {
      throw new UnAuthenticatedException("User not logged in.");
    }
    return authentication;
  }

  protected AuthUser getAuthUser() {
    final String token = getAuthentication().getToken().getTokenValue();
    return authUserCacheManager.getCachedUser(token);
  }

  protected String getCompanyId() {
    return getAuthUser().getCompanyId();
  }

  String getUserId() {
    return getAuthUser().getId();
  }
}
