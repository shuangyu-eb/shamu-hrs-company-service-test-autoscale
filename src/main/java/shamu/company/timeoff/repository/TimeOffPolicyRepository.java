package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.dto.TimeOffPolicyList;
import shamu.company.timeoff.entity.TimeOffPolicy;

public interface TimeOffPolicyRepository extends BaseRepository<TimeOffPolicy, Long> {

  @Query(value = "select p.id, p.name, count(pu.id) as employee, "
      + "p.is_limited as isLimited from time_off_policies p "
      + "left join time_off_policies_users pu on "
      + "p.id = pu.time_off_policy_id and pu.deleted_at is null "
      + "where p.deleted_at is null  "
      + "and p.company_id = ?1 "
      + "group by p.id", nativeQuery = true)
  List<TimeOffPolicyList> getAllPolicies(Long company);
}
