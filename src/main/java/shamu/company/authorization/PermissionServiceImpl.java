package shamu.company.authorization;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.repository.UserRepository;

@Service
public class PermissionServiceImpl implements PermissionService {

  private final PermissionRepository permissionRepository;

  private final UserRepository userRepository;

  @Autowired
  public PermissionServiceImpl(final PermissionRepository permissionRepository,
      final UserRepository userRepository) {
    this.permissionRepository = permissionRepository;
    this.userRepository = userRepository;
  }

  @Override
  public List<AuthorityPojo> getPermissionByUser(final User user) {
    final List<AuthorityPojo> authorities;
    final List<Permission> permissions = permissionRepository
        .findByUserRoleId(user.getUserRole().getId());

    if (user.getRole() == Role.MANAGER) {
      final List<Long> teamMembers = userRepository.findByManagerUser(user)
          .stream().map(User::getId).collect(Collectors.toList());

      authorities = permissions.stream().map(permission -> {
        AuthorityPojo permissionPojo = new AuthorityPojo(permission);

        if (permission.getName().getPermissionType()
            == Permission.PermissionType.MANAGER_PERMISSION) {
          permissionPojo.setIds(teamMembers);
        }

        return permissionPojo;
      }).collect(Collectors.toList());
    } else {
      authorities = permissions.stream().map(AuthorityPojo::new).collect(Collectors.toList());
    }

    return authorities;
  }
}
