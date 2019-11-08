package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import shamu.company.admin.dto.SuperAdminUserDto;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;

public interface UserRepository extends BaseRepository<User, Long>, UserCustomRepository {
  String ACTIVE_USER_QUERY = "and (u.deactivated_at is null "
       + "or (u.deactivated_at is not null "
       + "and u.deactivated_at > current_timestamp)) ";

  @Query(value =
          "select * from users u"
           + " where u.user_id = ?1 "
           + ACTIVE_USER_QUERY,
          nativeQuery = true)
  User findByUserId(String userId);

  @Query(value =
      "select * from users u"
        + " where u.user_id = ?1 ",
      nativeQuery = true)
  User findActiveAndDeactivatedUserByUserId(String userId);

  @Query(
      value =
          "select * from users u"
            + " where u.user_personal_information_id=?1 "
            + ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByUserPersonalInformationId(Long personalInformationId);

  @Query(
      value =
          "select * from users u where"
            + " u.user_contact_information_id=?1 "
            + ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByUserContactInformationId(Long contactInformationId);

  @Query(value = "select u.* from users u "
      + "left join user_contact_information uc on u.user_contact_information_id = uc.id "
      + "where uc.email_work = ?1 "
      + ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByEmailWork(String emailWork);

  @Query(
      value = "SELECT * FROM users u "
          + "WHERE u.manager_user_id = ?1 "
          + ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findAllByManagerUserId(Long id);

  List<User> findByManagerUser(User managerUser);

  @Query(
      value = "SELECT count(1) FROM users u"
        + " WHERE u.company_id = ?1 "
        + ACTIVE_USER_QUERY,
      nativeQuery = true)
  Integer findExistingUserCountByCompanyId(Long companyId);

  @Query(
      value = "SELECT * FROM users u"
          + " WHERE u.company_id = ?1 "
          + ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<User> findAllByCompanyId(Long companyId);

  Boolean existsByResetPasswordToken(String token);

  Boolean existsByChangeWorkEmailToken(String token);

  User findByResetPasswordToken(String token);

  @Query(
          value = "select * from users u"
                  + " where u.company_id = ?1 "
                  + ACTIVE_USER_QUERY,
          nativeQuery = true
  )
  List<User> findEmployersAndEmployeesByCompanyId(Long companyId);

  @Query(
          value = "select * from users u"
                  + " join jobs_users ju on u.id = ju.user_id"
                  + " where u.company_id = ?1"
                  + " and u.manager_user_id = ?2 "
                  + ACTIVE_USER_QUERY,
          nativeQuery = true
  )
  List<User> findDirectReportsEmployersAndEmployeesByCompanyId(
          Long companyId, Long userId);

  @Query(value = "select count(1) from users u "
      + "where u.manager_user_id = ?1 "
      + "and u.company_id = ?2 "
      + ACTIVE_USER_QUERY,
      nativeQuery = true)
  Integer findDirectReportsCount(Long orgUserId, Long companyId);

  @Query(value = "select u.manager_user_id from users u where u.id = ?1 "
      + ACTIVE_USER_QUERY,
      nativeQuery = true)
  Long getManagerUserIdById(Long userId);

  @Query(value = "select * from users u where u.id = ?1 and u.company_id = ?2 "
      + ACTIVE_USER_QUERY,
      nativeQuery = true)
  User findByIdAndCompanyId(Long userId, Long companyId);

  @Query(value =
      "SELECT new shamu.company.admin.dto.SuperAdminUserDto(u) "
          + "FROM User u "
          + "WHERE u.userStatus.name='ACTIVE' "
          + "AND ( "
          + " u.userPersonalInformation.firstName LIKE CONCAT('%',?1,'%') "
          + "OR u.userPersonalInformation.lastName LIKE CONCAT('%',?1,'%') "
          + "OR u.company.name  LIKE CONCAT('%',?1,'%') "
          + "OR u.userContactInformation.emailWork LIKE CONCAT('%',?1,'%') ) "
          + "AND (u.deactivatedAt is null "
          + "OR (u.deactivatedAt IS NOT NULL "
          + "AND u.deactivatedAt > current_timestamp ))")
  Page<SuperAdminUserDto> findBy(String keyword, Pageable pageable);


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
