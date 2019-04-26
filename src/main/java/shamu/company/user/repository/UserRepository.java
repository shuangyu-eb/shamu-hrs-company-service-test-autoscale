package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
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

  @Query(
      value = "SELECT count(1) FROM users WHERE company_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true)
  Integer findExistingUserCountByCompanyId(Long companyId);

  @Query(
      value =
          "SELECT new shamu.company.job.entity.JobUserListItem(u.id, u.imageUrl, "
              + "up.firstName, up.lastName, d.name, j.title) "
              + "FROM User u "
              + "LEFT JOIN u.job j "
              + "LEFT JOIN u.userPersonalInformation up "
              + "LEFT JOIN u.job.department d "
              + "WHERE j.deletedAt IS NULL AND u.deletedAt IS NULL "
              + "AND u.company.id = :companyId "
              + "AND (up.firstName "
              + "LIKE concat('%', :#{#employeeListSearchCondition.keyword}, '%') "
              + "OR up.lastName "
              + "LIKE concat('%', :#{#employeeListSearchCondition.keyword}, '%') "
              + "OR d.name LIKE "
              + "concat('%', :#{#employeeListSearchCondition.keyword}, '%') "
              + "OR j.title LIKE "
              + "concat('%', :#{#employeeListSearchCondition.keyword}, '%')) ")
  Page<JobUserListItem> getAllByCondition(
      @Param("employeeListSearchCondition") EmployeeListSearchCondition employeeListSearchCondition,
      @Param("companyId") Long companyId, Pageable pageable);
}
