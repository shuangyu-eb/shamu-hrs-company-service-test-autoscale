package shamu.company.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shamu.company.user.entity.User;

public interface UserRepository extends JpaRepository<User, String>, UserCustomRepository {

  @Override
  Optional<User> findById(String id);

  String ACTIVE_USER_QUERY = " and (u.deactivated_at is null "
       + "or (u.deactivated_at is not null "
       + "and u.deactivated_at > current_timestamp)) ";

  @Query(value =
          "select * from users u"
           + " where u.id = unhex(?1) "
           + ACTIVE_USER_QUERY,
          nativeQuery = true)
  User findActiveUserById(String userId);

  @Query(
      value =
          "select * from users u"
            + " where u.user_personal_information_id=unhex(?1) "
            + ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByUserPersonalInformationId(String personalInformationId);

  @Query(
      value =
          "select * from users u where"
            + " u.user_contact_information_id=unhex(?1) "
            + ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByUserContactInformationId(String contactInformationId);

  @Query(value = "select u.* from users u "
      + "left join user_contact_information uc on u.user_contact_information_id = uc.id "
      + "where uc.email_work = ?1 "
      + ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByEmailWork(String emailWork);

  @Query(
      value = "SELECT * FROM users u "
          + "WHERE u.manager_user_id = unhex(?1) "
          + ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findAllByManagerUserId(String id);

  List<User> findByManagerUser(User managerUser);

  @Query(
      value = "SELECT count(1) FROM users u"
        + " WHERE u.company_id = unhex(?1) "
        + ACTIVE_USER_QUERY,
      nativeQuery = true)
  Integer findExistingUserCountByCompanyId(String companyId);

  @Query(
      value = "SELECT * FROM users u"
          + " WHERE u.company_id = unhex(?1) "
          + ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findAllByCompanyId(String companyId);

  Boolean existsByResetPasswordToken(String token);

  Boolean existsByChangeWorkEmailToken(String token);

  User findByResetPasswordToken(String token);

  @Query(
          value = "select * from users u"
                  + " where u.company_id = unhex(?1) "
                  + ACTIVE_USER_QUERY,
          nativeQuery = true
  )
  List<User> findByCompanyId(String companyId);

  @Query(
          value = "select * from users u"
                  + " join jobs_users ju on u.id = ju.user_id"
                  + " where u.company_id = unhex(?1)"
                  + " and u.manager_user_id = unhex(?2) "
                  + ACTIVE_USER_QUERY,
          nativeQuery = true
  )
  List<User> findSubordinatesByManagerUserId(
      String companyId, String userId);

  @Query(value = "select count(1) from users u "
      + "where u.manager_user_id = unhex(?1) "
      + "and u.company_id = unhex(?2) "
      + ACTIVE_USER_QUERY,
      nativeQuery = true)
  Integer findDirectReportsCount(String orgUserId, String companyId);

  @Query(value = "select hex(u.manager_user_id) from users u where u.id = unhex(?1) "
      + ACTIVE_USER_QUERY,
      nativeQuery = true)
  String getManagerUserIdById(String userId);

  @Query(value = "select * from users u where u.id = unhex(?1) and u.company_id = unhex(?2) "
      + ACTIVE_USER_QUERY,
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
}
