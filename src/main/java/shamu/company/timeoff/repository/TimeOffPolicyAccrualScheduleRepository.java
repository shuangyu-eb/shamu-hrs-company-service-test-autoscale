package shamu.company.timeoff.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

public interface TimeOffPolicyAccrualScheduleRepository extends
    BaseRepository<TimeOffPolicyAccrualSchedule, Long> {

  Long findIdByTimeOffPolicy(TimeOffPolicy timeOffPolicy);

  TimeOffPolicyAccrualSchedule findAllByTimeOffPolicy(TimeOffPolicy timeOffPolicy);
}
