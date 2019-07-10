package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.User;

public interface TimeOffPolicyUserRepository extends BaseRepository<TimeOffPolicyUser, Long> {

  List<TimeOffPolicyUser> findTimeOffPolicyUsersByUser(User user);

  List<TimeOffPolicyUser> findAllByTimeOffPolicyId(Long timeOffPolicyId);

  @Query(value = "select sum(balance) "
      + "from time_off_policies_users "
      + "where user_id=?1 and deleted_at is null", nativeQuery = true)
  Integer getBalanceByUserId(Long userId);

  TimeOffPolicyUser findTimeOffPolicyUserByUserAndTimeOffPolicy(User user,
      TimeOffPolicy timeOffPolicy);

  Boolean existsByUserId(Long id);
}
