package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.pojo.OvertimePolicyOverviewPojo;
import shamu.company.common.repository.BaseRepository;

import java.util.List;

/** @author mshumaker */
public interface OvertimePolicyRepository extends BaseRepository<OvertimePolicy, String> {
  @Modifying
  @Query("update OvertimePolicy op set op.active = 0 where op.id =?1")
  void softDeleteOvertimePolicy(String overtimeId);

  OvertimePolicy findByDefaultPolicyIsTrue();

  OvertimePolicy findByPolicyName(String name);

  @Query(
      value =
          "select hex(op.id) as id, op.policy_name as policyName, "
              + "op.default_policy as defaultPolicy, count(distinct (c.user_id)) as numberOfEmployees "
              + "from overtime_policies op "
              + "left join "
              + "user_compensations c "
              + "on op.id=c.overtime_policy_id "
              + "and (c.end_date is null or c.end_date > current_timestamp) and c.start_date is not null "
              + "where op.active = 1 "
              + "GROUP BY id, policyName, defaultPolicy "
              + "ORDER BY policyName ",
      nativeQuery = true)
  List<OvertimePolicyOverviewPojo> findOvertimeOverview();

  @Query(value = "select policy_name from overtime_policies where active = 1", nativeQuery = true)
  List<String> findAllPolicyNames();

  @Modifying
  @Query(
      value =
          "update overtime_policies "
              + "set default_policy = 0 "
              + "where default_policy = 1 "
              + "AND  id != unhex(?1)",
      nativeQuery = true)
  void unsetOldDefaultOvertimePolices(String currentDefault);

  @Query(
      value = "select count(*) from overtime_policies " + "where active = 1 AND policy_name = ?1",
      nativeQuery = true)
  Integer countByPolicyName(String name);

  @Query(
          value = "select * from overtime_policies " + "where active = 1",
          nativeQuery = true)
  List<OvertimePolicy> findAllActive();
}
