package shamu.company.common;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;

public class BaseRestController {

  public User getUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      throw new UnAuthenticatedException("User not logged in.");
    }

    return (User) authentication.getPrincipal();
  }

  public Company getCompany() {
    return this.getUser().getCompany();
  }
}
