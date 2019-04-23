package shamu.company.authorization;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import shamu.company.user.entity.User;

public interface PermissionService {

  List<GrantedAuthority> getPermissionByUser(User user);
}
