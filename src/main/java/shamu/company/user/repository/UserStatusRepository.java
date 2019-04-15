package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserStatus;

public interface UserStatusRepository extends BaseRepository<UserStatus, Long> {

  @Query(value = "SELECT * FROM user_statuses WHERE name IN ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  List<UserStatus> findAllByName(List<String> nameList);
}
