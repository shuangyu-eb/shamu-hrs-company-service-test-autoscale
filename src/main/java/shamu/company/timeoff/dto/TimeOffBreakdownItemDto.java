package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.entity.TimeOffAdjustment;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.utils.DateUtil;

@Data
@NoArgsConstructor
@Builder
public class TimeOffBreakdownItemDto {

  private LocalDateTime date;

  private String detail;

  private Integer amount;

  private Integer balance;

  @JSONField(serialize = false)
  private BreakDownType breakdownType;

  public enum BreakDownType {
    TIME_OFF_ACCRUAL,
    TIME_OFF_ADJUSTMENT,
    TIME_OFF_REQUEST
  }

  public TimeOffBreakdownItemDto(LocalDateTime date, String detail,
      Integer amount, Integer balance, BreakDownType breakDownType) {
    this.date = date;
    this.detail = detail;
    this.amount = amount;
    this.balance = balance;
    this.breakdownType = breakDownType;
  }

  public TimeOffBreakdownItemDto(LocalDateTime date, String detail,
      Integer amount, BreakDownType breakDownType) {
    this.date = date;
    this.detail = detail;
    this.amount = amount;
    this.breakdownType = breakDownType;
  }

  @JSONField(serialize = false)
  public static TimeOffBreakdownItemDto fromTimeOffPolicyUser(TimeOffPolicyUser timeOffPolicyUser) {

    Integer startingBalance = timeOffPolicyUser.getBalance();
    if (startingBalance == null) {
      startingBalance = 0;
    }

    return new TimeOffBreakdownItemDto(
        DateUtil.toLocalDateTime(timeOffPolicyUser.getUpdatedAt()),
        "Your Starting Balance",
        startingBalance,
        startingBalance,
        BreakDownType.TIME_OFF_ACCRUAL
    );
  }

  @JSONField(serialize = false)
  public static TimeOffBreakdownItemDto fromTimeOffAdjustment(TimeOffAdjustment timeOffAdjustment) {
    return new TimeOffBreakdownItemDto(
        DateUtil.toLocalDateTime(timeOffAdjustment.getCreatedAt()),
        timeOffAdjustment.getComment(),
        timeOffAdjustment.getAmount(),
        BreakDownType.TIME_OFF_ADJUSTMENT
    );
  }
}
