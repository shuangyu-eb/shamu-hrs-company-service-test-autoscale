package shamu.company.timeoff.repository;

import java.math.BigInteger;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

public interface TimeOffPolicyAccrualScheduleRepository extends
    BaseRepository<TimeOffPolicyAccrualSchedule, Long> {

  @Query("select s from TimeOffPolicyAccrualSchedule s where s.deletedAt is null "
      + "and s.timeOffPolicy = ?1 "
      + "and s.expiredAt is null")
  TimeOffPolicyAccrualSchedule findByTimeOffPolicy(TimeOffPolicy timeOffPolicy);

  @Query(
      value = "select id from time_off_policy_accrual_schedules"
          + " where time_off_policy_id = ?1"
          + " and deleted_at is null",
      nativeQuery = true
  )
  List<BigInteger> findIdByTimeOffPolicyId(Long id);

  @Query("select s from TimeOffPolicyAccrualSchedule s where s.deletedAt is null "
      + "and s.timeOffPolicy = ?1")
  List<TimeOffPolicyAccrualSchedule> findAllWithExpiredTimeOffPolicy(TimeOffPolicy timeOffPolicy);
}
