package shamu.company.timeoff.pojo;

import java.util.List;
import lombok.Data;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;

@Data
public class TimeOffPolicyWrapperPojo {
  private TimeOffPolicyPojo timeOffPolicy;

  private TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualSchedule;

  private List<TimeOffPolicyUserPojo> userStartBalances;

  private List<AccrualScheduleMilestoneDto> milestones;
}
