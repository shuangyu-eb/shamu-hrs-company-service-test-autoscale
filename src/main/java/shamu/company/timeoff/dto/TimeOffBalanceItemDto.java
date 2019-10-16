package shamu.company.timeoff.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeOffBalanceItemDto {

  @HashidsFormat
  private Long id;

  private String name;

  private Integer currentBalance;

  private Integer approvalBalance;

  private Integer availableBalance;

  private boolean showBalance;
}
