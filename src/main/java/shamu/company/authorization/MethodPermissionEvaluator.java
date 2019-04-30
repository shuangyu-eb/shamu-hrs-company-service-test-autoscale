package shamu.company.authorization;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import shamu.company.common.exception.ForbiddenException;

@Component
public class MethodPermissionEvaluator implements PermissionEvaluator {

  private final PermissionUtils permissionUtils;

  @Autowired
  public MethodPermissionEvaluator(PermissionUtils permissionUtils) {
    this.permissionUtils = permissionUtils;
  }

  @Override
  public boolean hasPermission(Authentication auth, Object target, Object permission) {
    throw new ForbiddenException(
        "method hasPermission(Object target, Object permission) not allowed");
  }

  @Override
  public boolean hasPermission(Authentication auth, Serializable targetId, String targetType,
      Object permission) {
    Type type = Type.valueOf(targetType);
    Long id = (Long) targetId;

    Permission.Name permissionName = Permission.Name.valueOf(permission.toString());
    return permissionUtils.hasPermission(auth, id, type, permissionName);
  }
}
