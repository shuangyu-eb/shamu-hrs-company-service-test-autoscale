package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;

public interface UserRepository extends BaseRepository<User, Long>, UserCustomRepository {

  @Query(
      value =
          "select * from users u "
              + "left join user_statuses us on u.user_status_id = us.id "
              + "where u.deleted_at is null and u.user_id = ?1 and us.name = ?2",
      nativeQuery = true)
  User findByUserIdAndStatus(String userId, String userStatus);

  @Query(value = "select u from User u where u.deletedAt is null and u.userId = ?1")
  User findByUserId(String userId);

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

  @Query(value = "select u.* from users u "
      + "left join user_contact_information uc on u.user_contact_information_id = uc.id "
      + "where u.deleted_at is null and uc.email_work = ?1", nativeQuery = true)
  User findByEmailWork(String emailWork);

  Boolean existsByEmailWork(String email);

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

  @Query(
      value = "SELECT * FROM users "
          + "WHERE company_id = ?1 and deactivated_at is null AND deleted_at IS NULL",
      nativeQuery = true)
  List<User> findAllByCompanyId(Long companyId);

  Boolean existsByResetPasswordToken(String token);

  User findByResetPasswordToken(String token);

  @Query(
      value = "select * from users"
          + " where (users.id in (select user_id"
          + " from jobs_users"
          + " where job_id in (select id"
          + " from jobs"
          + " where department_id = ?1)"
          + " ) or company_id = ?2 "
          + " and manager_user_id is null) "
          + " and deactivated_at is null "
          + " and deleted_at is null ",
      nativeQuery = true
  )
  List<User> findEmployersAndEmployeesByDepartmentIdAndCompanyId(Long departmentId, Long companyId);

  @Query(value = "select count(1) from users u "
      + "where u.manager_user_id = ?1 "
      + "and u.deleted_at is null "
      + "and u.company_id = ?2", nativeQuery = true)
  Integer findDirectReportsCount(Long orgUserId, Long companyId);

  @Query(value = "select u.manager_user_id from users u where u.id = ?1 "
      + "and u.deleted_at is null", nativeQuery = true)
  Long getManagerUserIdById(Long userId);

  @Query(value = "select * from users u where u.deleted_at is null "
      + "and u.id = ?1 and u.company_id = ?2", nativeQuery = true)
  User findByIdAndCompanyId(Long userId, Long companyId);

  @Query(value =
      "SELECT new shamu.company.admin.dto.SuperAdminUserDto(u) "
          + "FROM User u "
          + "WHERE u.userStatus.name='ACTIVE' "
          + "AND u.userPersonalInformation.firstName LIKE CONCAT('%',?1,'%')")
  Page<SuperAdminUserDto> findBy(String keyword, Pageable pageable);

}
