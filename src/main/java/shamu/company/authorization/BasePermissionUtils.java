package shamu.company.authorization;

import shamu.company.common.config.SecurityHolder;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;

class BasePermissionUtils {

  protected User getUser() {
    final User user = SecurityHolder.getCurrentUser().get();
    if (user == null) {
      throw new UnAuthenticatedException("User does not logged in.");
    }
    return user;
  }

  protected Company getCompany() {
    return this.getUser().getCompany();
  }
}
