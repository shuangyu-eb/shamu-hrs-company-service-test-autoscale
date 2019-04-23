package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;

public interface UserRepository extends BaseRepository<User, Long> {

  User findByEmailWork(String emailWork);

  User findByVerificationToken(String activationToken);

  @Query(
      value = "SELECT * FROM users WHERE manager_user_id IS NOT NULL AND deleted_at IS NULL",
      nativeQuery = true)
  List<User> findAllEmployees();

  Boolean existsByEmailWork(String email);

  List<User> findByUserRoleAndCompany(UserRole userRole, Company company);


  @Query(value = "SELECT * FROM users "
      + "WHERE manager_user_id = ?1 AND deleted_at IS NULL", nativeQuery = true)
  List<User> findAllByManagerUserId(Long id);

  @Query(value = "SELECT * FROM users WHERE user_role_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  List<User> findByUserRoleId(Long id);
}
