package shamu.company.timeoff.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class TimeOffAdjustmentCheckDto {

  Integer maxBalance;

  Boolean exceed;
}
