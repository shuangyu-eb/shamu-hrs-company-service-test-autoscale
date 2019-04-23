package shamu.company.authorization;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class PermissionRestController extends BaseRestController {

  @Autowired
  PermissionService permissionService;

  @GetMapping("permissions")
  public List<String> getPermissions() {
    return permissionService.getPermissionByUser(this.getUser()).stream()
        .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
  }
}
