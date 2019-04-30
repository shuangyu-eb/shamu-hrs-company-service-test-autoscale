package shamu.company.authorization;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class PermissionRestController extends BaseRestController {

  private final PermissionService permissionService;

  @Autowired
  public PermissionRestController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @GetMapping("permissions")
  public List<AuthorityPojo> getPermissions() {
    return permissionService.getPermissionByUser(this.getUser());
  }
}
