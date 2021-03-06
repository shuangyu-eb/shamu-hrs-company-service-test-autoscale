package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TimeOffBalanceDto {

  private boolean showTotalBalance;

  private List<TimeOffBalanceItemDto> timeOffBalanceItemDtos;
}
