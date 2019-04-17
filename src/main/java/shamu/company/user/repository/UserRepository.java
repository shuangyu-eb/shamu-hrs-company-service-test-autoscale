package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;

public interface UserRepository extends BaseRepository<User, Long> {

  User findByEmailWork(String emailWork);

  User findByVerificationToken(String activationToken);

  @Query(
      value = "SELECT * FROM users WHERE manager_user_id IS NOT NULL AND deleted_at IS NULL",
      nativeQuery = true)
  List<User> findAllEmployees();

  Boolean existsByEmailWork(String email);

  @Query(value = "SELECT * FROM users WHERE manager_user_id = ?1 AND deleted_at IS NULL", nativeQuery = true)
  List<User> findAllByManagerUserId(Long mid);
}
