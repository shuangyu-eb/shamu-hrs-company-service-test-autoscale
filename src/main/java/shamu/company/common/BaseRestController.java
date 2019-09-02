package shamu.company.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.config.SecurityHolder;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;

public class BaseRestController {

  private DefaultJwtAuthenticationToken getAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (DefaultJwtAuthenticationToken) authentication;
  }

  private void checkIsAuthenticated() {
    if (SecurityHolder.getCurrentUser().get() == null) {
      throw new UnAuthenticatedException("User not logged in.");
    }
  }

  public User getUser() {
    checkIsAuthenticated();
    return SecurityHolder.getCurrentUser().get();
  }

  public String getUserId() {
    return getAuthentication().getId();
  }

  public Company getCompany() {
    return this.getUser().getCompany();
  }
}
