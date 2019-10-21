package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;

public interface AccrualScheduleMilestoneRepository extends
    BaseRepository<AccrualScheduleMilestone, Long> {

  @Query("select m from AccrualScheduleMilestone m where m.deletedAt is null "
      + "and m.timeOffPolicyAccrualScheduleId = ?1 "
      + "and m.expiredAt is null")
  List<AccrualScheduleMilestone> findByTimeOffPolicyAccrualScheduleId(Long id);

  @Query("select m from AccrualScheduleMilestone m where m.deletedAt is null "
          + "and m.timeOffPolicyAccrualScheduleId in ?1 "
          + "and m.expiredAt is null")
  List<AccrualScheduleMilestone> findByTimeOffPolicyAccrualScheduleIds(List<Long> id);

  @Modifying
  @Query(value = "update time_off_policy_accrual_schedule_milestones "
      + "set time_off_policy_accrual_schedule_id = ?2 "
      + "where time_off_policy_accrual_schedule_id = ?1 and deleted_at is null", nativeQuery = true)
  void updateMilestoneSchedule(Long originScheduleId, Long newScheduleId);
}
