package shamu.company.attendance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.PolicyDetail;
import shamu.company.common.repository.BaseRepository;

public interface PolicyDetailRepository extends BaseRepository<PolicyDetail, String> {
  List<PolicyDetail> findAllByOvertimePolicyId(String policyId);

  @Modifying
  @Query("update OvertimePolicy op set op.active = 0 where op.id =?1")
  void softDeleteOvertimePolicy(String overtimeId);
}
