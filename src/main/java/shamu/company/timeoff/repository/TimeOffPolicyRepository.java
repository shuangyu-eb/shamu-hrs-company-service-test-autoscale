package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.pojo.TimeOffPolicyListPojo;

public interface TimeOffPolicyRepository extends BaseRepository<TimeOffPolicy, Long> {

  @Query(value = "select p.id, p.name, count(pu.id) as employee, "
      + "p.is_limited as isLimited from time_off_policies p "
      + "left join time_off_policies_users pu on "
      + "p.id = pu.time_off_policy_id "
      + "where p.company_id = ?1 "
      + "group by p.id", nativeQuery = true)
  List<TimeOffPolicyListPojo> getAllPolicies(Long company);
}
