package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.pojo.TimeOffPolicyListPojo;

public interface TimeOffPolicyRepository extends BaseRepository<TimeOffPolicy, String> {

  @Query(
      value =
          "select hex(p.id) as id, p.name, count(pu.id) as employee, "
              + "p.is_limited as isLimited from time_off_policies p "
              + "left join time_off_policies_users pu on "
              + "p.id = pu.time_off_policy_id "
              + "group by id, name, isLimited order by name ASC",
      nativeQuery = true)
  List<TimeOffPolicyListPojo> getAllPolicies();

  @Query(
      value = "SELECT count(1) FROM time_off_policies top" + " WHERE binary top.name = ?1 ",
      nativeQuery = true)
  Integer countByName(String policyName);

  List<TimeOffPolicy> findByIsAutoEnrollEnabledIsTrue();
}
