package shamu.company.authorization;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import shamu.company.common.exception.ForbiddenException;

@Component
public class MethodPermissionEvaluator implements PermissionEvaluator {

  private final PermissionUtils permissionUtils;

  @Autowired
  public MethodPermissionEvaluator(final PermissionUtils permissionUtils) {
    this.permissionUtils = permissionUtils;
  }

  @Override
  public boolean hasPermission(
      final Authentication auth, final Object target, final Object permission) {
    throw new ForbiddenException(
        "method hasPermission(Object target, Object permission) not allowed");
  }

  @Override
  public boolean hasPermission(
      final Authentication auth,
      final Serializable target,
      final String targetType,
      final Object permission) {
    final Type type = Type.valueOf(targetType);
    final Permission.Name permissionName = Permission.Name.valueOf(permission.toString());

    if (target instanceof String) {
      final String id = (String) target;
      return permissionUtils.hasPermission(auth, id, type, permissionName);
    }

    if (target instanceof List) {
      final List targets = (List) target;
      return permissionUtils.hasPermission(auth, targets, type, permissionName);
    }

    return permissionUtils.hasPermission(auth, target, type, permissionName);
  }
}
