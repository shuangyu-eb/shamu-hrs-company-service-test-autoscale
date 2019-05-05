package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;

public interface UserRepository extends BaseRepository<User, Long>, UserCustomRepository {

  @Query(
      value =
          "select * from users u "
              + "left join user_statuses us on u.user_status_id = us.id "
              + "where u.deleted_at is null and u.email_work = ?1 and us.name = ?2",
      nativeQuery = true)
  User findByEmailWorkAndStatus(String emailWork, String userStatus);

  User findByEmailWork(String emailWork);

  User findByVerificationToken(String activationToken);

  @Query(
      value = "select * from users where user_personal_information_id=?1 and deleted_at is null",
      nativeQuery = true)
  User findByUserPersonalInformationId(Long personalInformationId);

  @Query(
      value = "select * from users where user_contact_information_id=?1 and deleted_at is null",
      nativeQuery = true)
  User findByUserContactInformationId(Long contactInformationId);

  List<User> findByCompany(Company company);

  Boolean existsByEmailWork(String email);

  List<User> findByUserRoleAndCompany(UserRole userRole, Company company);

  @Query(
      value = "SELECT * FROM users " + "WHERE manager_user_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  List<User> findAllByManagerUserId(Long id);

  @Query(
      value = "SELECT * FROM users WHERE user_role_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  List<User> findByUserRoleId(Long id);

  List<User> findByManagerUser(User managerUser);

  @Query(
      value = "SELECT count(1) FROM users WHERE company_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  Integer findExistingUserCountByCompanyId(Long companyId);
}
