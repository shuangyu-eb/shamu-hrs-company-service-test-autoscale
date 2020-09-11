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

  OvertimePolicy findByCompanyIdAndDefaultPolicyIsTrue(String companyId);

  List<OvertimePolicy> findByCompanyId(String companyId);

  @Query(
      value =
          "select hex(op.id) as id, op.policy_name as policyName,"
              + " op.default_policy as defaultPolicy, count(c.id) as numberOfEmployees "
              + "from overtime_policies op "
              + "left join "
              + "user_compensations c "
              + "on op.id=c.overtime_policy_id "
              + "where hex(op.company_id) = ?1 AND op.active = 1 "
              + "GROUP BY id, policyName, defaultPolicy",
      nativeQuery = true)
  List<OvertimePolicyOverviewPojo> findOvertimeOverview(String companyId);

  @Query(
      value =
          "select policy_name from overtime_policies where company_id = unhex(?1) and "
              + "active = 1",
      nativeQuery = true)
  List<String> findAllPolicyNames(String companyId);
}
