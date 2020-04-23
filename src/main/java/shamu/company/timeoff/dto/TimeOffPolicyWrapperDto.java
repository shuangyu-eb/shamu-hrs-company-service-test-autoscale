package shamu.company.timeoff.dto;

import java.util.List;
import javax.validation.Valid;
import lombok.Data;

@Data
public class TimeOffPolicyWrapperDto {
  @Valid private TimeOffPolicyFrontendDto timeOffPolicy;

  private TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualSchedule;

  private List<TimeOffPolicyUserFrontendDto> userStartBalances;

  private List<AccrualScheduleMilestoneDto> milestones;
}
