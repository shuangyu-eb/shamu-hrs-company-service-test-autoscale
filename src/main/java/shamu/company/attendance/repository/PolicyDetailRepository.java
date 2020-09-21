package shamu.company.attendance.repository;

import shamu.company.attendance.entity.PolicyDetail;
import shamu.company.common.repository.BaseRepository;

import java.util.List;

public interface PolicyDetailRepository extends BaseRepository<PolicyDetail, String> {
  List<PolicyDetail> findAllByOvertimePolicyId(String policyId);

  void deleteByOvertimePolicyId(String policyId);
}
