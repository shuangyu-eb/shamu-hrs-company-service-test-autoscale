package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

public interface TimeOffPolicyAccrualScheduleRepository
    extends BaseRepository<TimeOffPolicyAccrualSchedule, String> {

  @Query(
      "select s from TimeOffPolicyAccrualSchedule s where s.timeOffPolicy = ?1 "
          + "and s.expiredAt is null")
  TimeOffPolicyAccrualSchedule findByTimeOffPolicy(TimeOffPolicy timeOffPolicy);

  @Query(
      value =
          "select hex(id) from time_off_policy_accrual_schedules"
              + " where time_off_policy_id = unhex(?1)",
      nativeQuery = true)
  List<String> findIdByTimeOffPolicyId(String id);

  @Query("select s from TimeOffPolicyAccrualSchedule s where s.timeOffPolicy = ?1")
  List<TimeOffPolicyAccrualSchedule> findAllWithExpiredTimeOffPolicy(TimeOffPolicy timeOffPolicy);
}
