package shamu.company.user.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserCompensation;

import java.util.List;

public interface UserCompensationRepository extends BaseRepository<UserCompensation, String> {
  Boolean existsByUserId(String userId);

  @Query(
      value =
          "select * from user_compensations "
              + "where user_id = unhex(?1) order by created_at desc limit 1 ",
      nativeQuery = true)
  UserCompensation findByUserId(String userId);

  @Query(
      value =
          "SELECT uc.* FROM user_compensations uc "
              + "JOIN (SELECT max(created_at) as created_at, user_id as user_id "
              + "FROM user_compensations GROUP BY user_id) as temp "
              + "ON uc.user_id = temp.user_id AND uc.created_at = temp.created_at AND hex(uc.user_id) in ?1 ",
      nativeQuery = true)
  List<UserCompensation> findByUserIdIn(List<String> userIds);

  @Query(
      value =
          "SELECT ucb.* FROM (SELECT max(uca.created_at) as created_at, uca.user_id as user_id "
              + "FROM user_compensations uca JOIN users u ON "
              + "u.id = uca.user_id "
              + "JOIN employees_ta_settings ets ON "
              + "uca.user_id = ets.employee_id "
              + "GROUP BY uca.user_id) as temp "
              + "JOIN user_compensations ucb "
              + "ON ucb.user_id = temp.user_id AND ucb.created_at = temp.created_at",
      nativeQuery = true)
  List<UserCompensation> listNewestEnrolledUserCompensationByCompanyId();

  @Query(
      value =
          "SELECT * from user_compensations where hex(overtime_policy_id) = ?1 "
              + "and end_date is null or end_date > current_timestamp ",
      nativeQuery = true)
  List<UserCompensation> findByOvertimePolicyId(String policyId);
}
