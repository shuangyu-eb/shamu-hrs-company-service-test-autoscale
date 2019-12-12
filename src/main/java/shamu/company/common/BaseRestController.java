package shamu.company.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;

public class BaseRestController {

  @Autowired
  private AuthUserCacheManager authUserCacheManager;

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
    return this.findAuthUser().getCompanyId();
  }
}
