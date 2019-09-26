package shamu.company.authorization;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PermissionUtils {

  private final UserPermissionUtils userPermissionUtils;

  private final CompanyPermissionUtils companyPermissionUtils;

  @Autowired
  public PermissionUtils(final UserPermissionUtils userPermissionUtils,
      final CompanyPermissionUtils companyPermissionUtils) {
    this.userPermissionUtils = userPermissionUtils;
    this.companyPermissionUtils = companyPermissionUtils;
  }

  boolean hasPermission(final Authentication auth, final Long targetId, final Type targetType,
      final Permission.Name permission) {
    return userPermissionUtils.hasPermission(auth, targetId, targetType, permission);
  }

  boolean hasPermission(final Authentication auth, final List target, final Type type,
      final Permission.Name permission) {
    return userPermissionUtils.hasPermission(auth, target, type, permission);
  }

  public boolean isMember(final String targetType, final Long id) {
    return companyPermissionUtils.isMember(targetType, id);
  }

  /**
   * @param userId The user id
   * @return true | false
   *
   * Usage: @PreAuthorize("@permissionUtils.isMember(#id)")
   */
  public boolean isMember(final Long userId) {
    return companyPermissionUtils.isMember(userId);
  }

  public boolean isCurrentUser(final Long id) {
    return userPermissionUtils.isCurrentUser(id);
  }

  public boolean isCurrentUser(final String email) {
    return userPermissionUtils.isCurrentUser(email);
  }
}
