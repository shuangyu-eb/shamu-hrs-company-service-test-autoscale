package shamu.company.timeoff.pojo;

import java.util.List;
import lombok.Data;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;

@Data
public class TimeOffPolicyPojo {
  private TimeOffPolicyDto timeOffPolicy;

  private TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualSchedule;

  private List<TimeOffPolicyUserDto> userStartBalances;

  private List<AccrualScheduleMilestoneDto> milestones;
}
