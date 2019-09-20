package shamu.company.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.AuthUser;

public class BaseRestController {

  @Autowired
  private AuthUserCacheManager authUserCacheManager;

  private DefaultJwtAuthenticationToken getAuthentication() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (DefaultJwtAuthenticationToken) authentication;
  }

  protected String getToken() {
    return getAuthentication().getToken().getTokenValue();

  }

  public AuthUser getAuthUser() {
    final String token = getToken();
    if (authUserCacheManager.getCachedUser(token) == null) {
      throw new UnAuthenticatedException("User not logged in.");
    }

    return getAuthentication().getAuthUser();
  }

  public String getUserId() {
    return getAuthentication().getUserId();
  }

  public Long getCompanyId() {
    return this.getAuthUser().getCompanyId();
  }
}
