package shamu.company.common.config;

import shamu.company.user.entity.User;

public class SecurityHolder {
  private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

  public static ThreadLocal<User> getCurrentUser() {
    return currentUser;
  }
}
