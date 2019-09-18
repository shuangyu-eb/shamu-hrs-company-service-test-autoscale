package shamu.company.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.AuthUser;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;

public class BaseRestController {

  @Autowired
  private AuthUserCacheManager authUserCacheManager;

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

  public AuthUser getAuthUser() {
    checkIsAuthenticated();
    String token = getAuthentication().getToken().getTokenValue();
    return authUserCacheManager.getCachedUser(token);
  }

  public String getUserId() {
    return getAuthentication().getId();
  }

  public Long getCompanyId() {
    return this.getAuthUser().getCompanyId();
  }
}
