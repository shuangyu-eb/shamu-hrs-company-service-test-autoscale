package shamu.company.timeoff.dto;

import java.util.List;
import lombok.Data;

@Data
public class TimeOffPolicyWrapperDto {

  private TimeOffPolicyFrontendDto timeOffPolicy;

  private TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualSchedule;

  private List<TimeOffPolicyUserFrontendDto> userStartBalances;

  private List<AccrualScheduleMilestoneDto> milestones;
}
