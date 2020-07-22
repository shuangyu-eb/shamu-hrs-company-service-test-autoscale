package shamu.company.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shamu.company.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, UserCustomRepository {

  String ACTIVE_USER_QUERY =
      "  (u.deactivated_at is null "
          + "or (u.deactivated_at is not null "
          + "and u.deactivated_at > current_timestamp)) ";

  String AND_ACTIVE_USER_QUERY = " and " + ACTIVE_USER_QUERY;

  String FIND_BY_PERIOD_AND_TIME_SHEET_STATUS =
      "select u.* from users u join timesheets t "
          + "on u.id = t.employee_id and t.time_period_id = unhex(?1) "
          + "join static_timesheet_status sts "
          + "on sts.name = (?2) and t.status_id = sts.id ";

  @Override
  Optional<User> findById(String id);

  List<User> findByIdIn(List<String> idList);

  @Query(
      value = "select * from users u" + " where u.id = unhex(?1) " + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findActiveUserById(String userId);

  @Query(
      value = "select * from users u" + " where u.user_personal_information_id=unhex(?1) ",
      nativeQuery = true)
  User findByUserPersonalInformationId(String personalInformationId);

  @Query(
      value = "select * from users u where" + " u.user_contact_information_id=unhex(?1) ",
      nativeQuery = true)
  User findByUserContactInformationId(String contactInformationId);

  @Query(
      value =
          "select u.* from users u "
              + "left join user_contact_information uc on u.user_contact_information_id = uc.id "
              + "where uc.email_work = ?1 "
              + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByEmailWork(String emailWork);

  @Query(
      value = "SELECT * FROM users u " + "WHERE u.manager_user_id = unhex(?1) " + ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findAllByManagerUserId(String id);

  List<User> findByManagerUser(User managerUser);

  @Query(
      value =
          "SELECT count(1) FROM users u"
              + " WHERE u.company_id = unhex(?1) "
              + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  Integer findExistingUserCountByCompanyId(String companyId);

  @Query(
      value = "SELECT * FROM users u" + " WHERE u.company_id = unhex(?1) " + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findAllByCompanyId(String companyId);

  List<User> findAllByCompanyIdAndIdNotIn(String companyId, List<String> userIdList);

  Boolean existsByResetPasswordToken(String token);

  User findByInvitationEmailToken(String token);

  Boolean existsByChangeWorkEmailToken(String token);

  User findByResetPasswordToken(String token);

  @Query(
      value = "select * from users u" + " where u.company_id = unhex(?1) " + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findByCompanyId(String companyId);

  @Query(value = "select * from users u where " + ACTIVE_USER_QUERY, nativeQuery = true)
  List<User> findAllActiveUsers();

  @Query(
      value =
          "select * from users u"
              + " join jobs_users ju on u.id = ju.user_id"
              + " where u.company_id = unhex(?1)"
              + " and u.manager_user_id = unhex(?2) "
              + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findSubordinatesByManagerUserId(String companyId, String userId);

  @Query(
      value =
          "select count(1) from users u "
              + "where u.manager_user_id = unhex(?1) "
              + "and u.company_id = unhex(?2) "
              + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  Integer findDirectReportsCount(String orgUserId, String companyId);

  @Query(
      value =
          "select hex(u.manager_user_id) from users u where u.id = unhex(?1) "
              + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  String getManagerUserIdById(String userId);

  @Query(
      value =
          "select * from users u where u.id = unhex(?1) and u.company_id = unhex(?2) "
              + AND_ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByIdAndCompanyId(String userId, String companyId);

  User findByChangeWorkEmailToken(String token);

  @Override
  default User save(final User user) {
    return saveUser(user);
  }

  @Override
  default List<User> saveAll(final Iterable users) {
    return saveAllUsers(users);
  }

  @Query(
      value =
          "select * from users u "
              + "where u.manager_user_id is null order by u.created_at limit 1",
      nativeQuery = true)
  User findSuperUser(String companyId);

  @Query(
      value =
          "select * from users u left join user_roles ur on u.user_role_id = ur.id where u.company_id = unhex(?1) and ur.name = ?2",
      nativeQuery = true)
  List<User> findUsersByCompanyIdAndUserRole(String companyId, String userRole);

  @Query(
      value =
          "select distinct u.* from users u join employees_ta_settings t on u.id = t.employee_id "
              + "and u.company_id = unhex(?1)",
      nativeQuery = true)
  List<User> findAttendanceEnrolledUsersByCompanyId(String companyId);

  @Query(value = FIND_BY_PERIOD_AND_TIME_SHEET_STATUS, nativeQuery = true)
  List<User> findUsersByPeriodIdAndTimeSheetStatus(String periodId, String timeSheetStatus);

  @Query(
      value =
          FIND_BY_PERIOD_AND_TIME_SHEET_STATUS
              + "join employees_ta_settings ets "
              + "on u.id = ets.employee_id and messaging_on = (?3) ",
      nativeQuery = true)
  List<User> findUsersByPeriodIdAndTimeSheetStatusAndMessageOn(
      String periodId, String timeSheetStatus, int messageOn);

  @Query(
      value =
          "select u1.* from users u1 where u1.id in "
              + "(select distinct u2.manager_user_id as manager_id from users u2 join timesheets t "
              + "on u2.id = t.employee_id and t.time_period_id = unhex(?1) "
              + "join static_timesheet_status sts "
              + "on sts.name = (?2) and t.status_id = sts.id) ",
      nativeQuery = true)
  List<User> findManagersByPeriodIdAndTimeSheetStatus(String periodId, String timeSheetStatus);

  List<User> findAllByTimeZoneIsNull();
}
