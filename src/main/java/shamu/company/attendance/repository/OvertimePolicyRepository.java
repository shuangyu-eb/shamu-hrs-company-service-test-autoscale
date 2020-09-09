package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.common.repository.BaseRepository;

/** @author mshumaker */
public interface OvertimePolicyRepository extends BaseRepository<OvertimePolicy, String> {
  @Modifying
  @Query("update OvertimePolicy op set op.active = 0 where op.id =?1")
  void softDeleteOvertimePolicy(String overtimeId);

  OvertimePolicy findByCompanyIdAndDefaultPolicyIsTrue(String companyId);
}
