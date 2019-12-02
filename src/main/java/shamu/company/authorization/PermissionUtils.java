package shamu.company.authorization;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PermissionUtils {

  private final UserPermissionUtils userPermissionUtils;

  @Autowired
  public PermissionUtils(final UserPermissionUtils userPermissionUtils) {
    this.userPermissionUtils = userPermissionUtils;
  }

  boolean hasPermission(final Authentication auth, final String targetId, final Type targetType,
      final Permission.Name permission) {
    return userPermissionUtils.hasPermission(auth, targetId, targetType, permission);
  }

  boolean hasPermission(final Authentication auth, final List target, final Type type,
      final Permission.Name permission) {
    return userPermissionUtils.hasPermission(auth, target, type, permission);
  }

  public boolean isCurrentUserId(final String id) {
    return userPermissionUtils.isCurrentUserId(id);
  }

  public boolean isCurrentUserEmail(final String email) {
    return userPermissionUtils.isCurrentUserEmail(email);
  }

  public boolean hasAuthority(final String permission) {
    return userPermissionUtils.hasAuthority(permission);
  }
}
