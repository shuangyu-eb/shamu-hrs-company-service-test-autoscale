package shamu.company.authorization;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;

public interface PermissionRepository extends BaseRepository<Permission, Long> {

  @Query(
      value = "SELECT p.* "
          + "FROM permission_types As pt, permissions AS p, permission_types_user_roles AS ptur "
          + "WHERE p.permission_type_id=pt.id AND "
          + "ptur.permission_type_id=pt.id AND ptur.user_role_id=?1 AND p.deleted_at IS NULL",
      nativeQuery = true)
  List<Permission> findByUserRoleId(Long userRoleId);
}
