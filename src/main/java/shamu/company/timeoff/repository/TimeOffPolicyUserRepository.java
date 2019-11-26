package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.User;

public interface TimeOffPolicyUserRepository extends BaseRepository<TimeOffPolicyUser, String> {

  List<TimeOffPolicyUser> findTimeOffPolicyUsersByUser(User user);

  List<TimeOffPolicyUser> findAllByTimeOffPolicyId(String timeOffPolicyId);

  @Query(value = "select sum(initial_balance) "
      + "from time_off_policies_users "
      + "where user_id=unhex(?1)", nativeQuery = true)
  Integer getBalanceByUserId(String userId);

  TimeOffPolicyUser findTimeOffPolicyUserByUserAndTimeOffPolicy(User user,
      TimeOffPolicy timeOffPolicy);

  Boolean existsByUserId(String id);
}
