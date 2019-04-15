package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.BaseRepository;
import shamu.company.user.entity.UserRole;

public interface UserRoleRepository extends BaseRepository<UserRole, Long> {

  @Query(value = "SELECT * FROM user_roles WHERE name IN ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  List<UserRole> findAllByName(List<String> nameList);
}
