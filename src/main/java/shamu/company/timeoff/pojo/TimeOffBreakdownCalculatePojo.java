package shamu.company.timeoff.pojo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;

@Data
@Builder
@NoArgsConstructor
public class TimeOffBreakdownCalculatePojo {

  private List<TimeOffPolicyAccrualSchedule> trimmedScheduleList;

  private List<TimeOffBreakdownItemDto> balanceAdjustment;

  private TimeOffPolicyUser policyUser;

  private LocalDateTime untilDate;
}
