package shamu.company.timeoff.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;

public interface AccrualScheduleMilestoneRepository extends
    BaseRepository<AccrualScheduleMilestone, Long> {

  List<AccrualScheduleMilestone> findByTimeOffPolicyAccrualScheduleId(Long id);
}
