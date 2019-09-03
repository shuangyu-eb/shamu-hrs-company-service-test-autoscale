package shamu.company.authorization;

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Component;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@Component
public class CompanyPermissionUtils extends BasePermissionUtils {

  private final UserService userService;

  public CompanyPermissionUtils(final UserService userService) {
    this.userService = userService;
  }

  boolean isMember(final String targetType, final Long id) {
    if (StringUtils.isBlank(targetType)) {
      return isMember(id);
    }
    return false;
  }

  boolean isMember(final Long userId) {
    final User targetUser = userService.findUserById(userId);
    if (targetUser != null && getUser() != null) {
      return targetUser.getCompany().getId().equals(getCompany().getId());
    }
    return false;
  }
}