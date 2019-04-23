package shamu.company.authorization;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.repository.UserRepository;

@Service
public class PermissionServiceImpl implements PermissionService {

  @Autowired
  PermissionRepository permissionRepository;

  @Autowired
  UserRepository userRepository;

  @Override
  public List<GrantedAuthority> getPermissionByUser(User user) {
    List<GrantedAuthority> authorities;
    List<Permission> permissions = permissionRepository
        .findByUserRoleId(user.getUserRole().getId());

    if (user.getRole() == Role.MANAGER) {
      List<Long> teamMembers = userRepository.findByManagerUser(user)
          .stream().map(User::getId).collect(Collectors.toList());

      authorities = permissions.stream().map(permission -> {
        String permissionName = permission.getName().toString();
        if (permission.getName().getPermissionType()
            == Permission.PermissionType.MANAGER_PERMISSION) {
          permissionName = permissionName + ":" + teamMembers.toString();
        }

        return new SimpleGrantedAuthority(permissionName);
      }).collect(Collectors.toList());
    } else {
      authorities = permissions.stream()
          .map(permission -> new SimpleGrantedAuthority(permission.getName().toString()))
          .collect(Collectors.toList());
    }

    return authorities;
  }
}
