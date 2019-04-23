package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserRole;

public interface UserRepository extends BaseRepository<User, Long> {

  User findByEmailWork(String emailWork);

  User findByVerificationToken(String activationToken);

  User findByUserContactInformation(UserContactInformation contactInformation);

  @Query(
      value = "select * from users where user_personal_information_id=?1 and deleted_at is null",
      nativeQuery = true)
  User findByUserPersonalInformationId(Long personalInformationId);

  List<User> findByCompany(Company company);

  Boolean existsByEmailWork(String email);

  List<User> findByUserRoleAndCompany(UserRole userRole, Company company);


  @Query(value = "SELECT * FROM users "
      + "WHERE manager_user_id = ?1 AND deleted_at IS NULL", nativeQuery = true)
  List<User> findAllByManagerUserId(Long id);

  @Query(value = "SELECT * FROM users WHERE user_role_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  List<User> findByUserRoleId(Long id);


  List<User> findByManagerUser(User managerUser);
}
