package shamu.company.authorization;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.entity.User;

@RestApiController
public class PermissionRestController extends BaseRestController {

  private final PermissionService permissionService;

  @Autowired
  public PermissionRestController(final PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @GetMapping("permissions")
  public AuthorityDto getPermissions() {
    final User user = this.getUser();
    final List<AuthorityPojo> authorities = permissionService.getPermissionByUser(user);

    final AuthorityDto authorityDto = new AuthorityDto(user);
    authorityDto.setAuthorities(authorities);
    return authorityDto;
  }
}
