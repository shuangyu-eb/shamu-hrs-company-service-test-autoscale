package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;

public interface AccrualScheduleMilestoneRepository extends
    BaseRepository<AccrualScheduleMilestone, Long> {

  @Query("select m from AccrualScheduleMilestone m where m.timeOffPolicyAccrualScheduleId = ?1 "
      + "and m.expiredAt is null")
  List<AccrualScheduleMilestone> findByTimeOffPolicyAccrualScheduleId(Long id);

  @Query("select m from AccrualScheduleMilestone m where m.timeOffPolicyAccrualScheduleId = ?1")
  List<AccrualScheduleMilestone> findByAccrualScheduleIdWithExpired(Long id);

  @Modifying
  @Query(value = "update time_off_policy_accrual_schedule_milestones "
      + "set time_off_policy_accrual_schedule_id = ?2 "
      + "where time_off_policy_accrual_schedule_id = ?1", nativeQuery = true)
  void updateMilestoneSchedule(Long originScheduleId, Long newScheduleId);

  @Query("select m from AccrualScheduleMilestone m where m.timeOffPolicyAccrualScheduleId = ?1 "
      + "and m.anniversaryYear <= ?2 and m.expiredAt is null")
  List<AccrualScheduleMilestone> findByAccrualScheduleIdAndEndYear(Long id, Integer maxYears);
}
