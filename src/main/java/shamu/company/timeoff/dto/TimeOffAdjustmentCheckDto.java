package shamu.company.timeoff.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeOffAdjustmentCheckDto {

  Integer maxBalance;

  Boolean exceed;
}
