package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.dto.TimeOffBalance;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.User;

public interface TimeOffPolicyUserRepository extends BaseRepository<TimeOffPolicyUser, Long> {

  @Query(value = "select policy.id as id, policy.name as name, policyUser.balance as balance "
      + "from time_off_policies_users policyUser "
      + "left join  time_off_policies policy  on policyUser.time_off_policy_id = policy.id "
      + "left join users user on policyUser.user_id = user.id "
      + "where user.deleted_at is null and user.id = ?1 and user.company_id = ?2 "
      + "order by balance desc", nativeQuery = true)
  List<TimeOffBalance> findTimeOffBalancesByUser(Long userId, Long companyId);

  List<TimeOffPolicyUser> findTimeOffPolicyUsersByUser(User user);

  @Query(value = "select sum(balance) "
      + "from time_off_policies_users "
      + "where user_id=?1 and deleted_at is null", nativeQuery = true)
  Integer getBalanceByUserId(Long userId);


  TimeOffPolicyUser findTimeOffPolicyUserByUserAndTimeOffPolicy(User user,
      TimeOffPolicy timeOffPolicy);
}
