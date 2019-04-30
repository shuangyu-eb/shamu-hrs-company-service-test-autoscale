package shamu.company.authorization;

import java.util.List;
import shamu.company.user.entity.User;

public interface PermissionService {

  List<AuthorityPojo> getPermissionByUser(User user);
}
