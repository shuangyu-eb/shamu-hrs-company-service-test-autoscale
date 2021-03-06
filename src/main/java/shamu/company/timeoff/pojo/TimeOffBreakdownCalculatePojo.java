package shamu.company.timeoff.pojo;

import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeOffBreakdownCalculatePojo {

  private List<TimeOffPolicyAccrualSchedule> trimmedScheduleList;

  private List<TimeOffBreakdownItemDto> balanceAdjustment;

  private TimeOffPolicyUser policyUser;

  private LocalDate untilDate;
}
