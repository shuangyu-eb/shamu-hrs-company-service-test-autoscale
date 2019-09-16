package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffAdjustmentPojo;
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

  public TimeOffBreakdownItemDto(final LocalDateTime date, final String detail,
      final Integer amount, final Integer balance, final BreakDownType breakdownType) {
    this.date = date;
    this.detail = detail;
    this.amount = amount;
    this.balance = balance;
    this.breakdownType = breakdownType;
  }

  public TimeOffBreakdownItemDto(final LocalDateTime date, final String detail,
      final Integer amount, final BreakDownType breakdownType) {
    this.date = date;
    this.detail = detail;
    this.amount = amount;
    this.breakdownType = breakdownType;
  }

  @JSONField(serialize = false)
  public static TimeOffBreakdownItemDto fromTimeOffPolicyUser(
      final TimeOffPolicyUser timeOffPolicyUser) {

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
  public static TimeOffBreakdownItemDto fromTimeOffAdjustment(
      final TimeOffAdjustmentPojo timeOffAdjustmentPojo) {
    return new TimeOffBreakdownItemDto(
        DateUtil.toLocalDateTime(timeOffAdjustmentPojo.getCreatedAt()),
        timeOffAdjustmentPojo.getComment(),
        timeOffAdjustmentPojo.getAmount(),
        BreakDownType.TIME_OFF_ADJUSTMENT
    );
  }
}
