package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  private String dateMessage;

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

  public TimeOffBreakdownItemDto(final LocalDateTime date, final String dateMessage,
                                 final String detail, final Integer amount,
                                 final Integer balance, final BreakDownType breakdownType) {
    this.date = date;
    this.dateMessage = dateMessage;
    this.detail = detail;
    this.amount = amount;
    this.balance = balance;
    this.breakdownType = breakdownType;
  }

  public TimeOffBreakdownItemDto(final LocalDateTime date, final String dateMessage,
                                 final String detail, final Integer amount,
                                 final BreakDownType breakdownType) {
    this.date = date;
    this.dateMessage = dateMessage;
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

    final String dateMessage = dateFormatConvert(
            DateUtil.toLocalDateTime(timeOffPolicyUser.getUpdatedAt()));

    return new TimeOffBreakdownItemDto(
        DateUtil.toLocalDateTime(timeOffPolicyUser.getUpdatedAt()),
        dateMessage,
        "Starting Balance",
        startingBalance,
        startingBalance,
        BreakDownType.TIME_OFF_ACCRUAL
    );
  }

  @JSONField(serialize = false)
  public static TimeOffBreakdownItemDto fromTimeOffAdjustment(
      final TimeOffAdjustmentPojo timeOffAdjustmentPojo) {
    final String dateMessage = dateFormatConvert(
            DateUtil.toLocalDateTime(timeOffAdjustmentPojo.getCreatedAt()));

    return new TimeOffBreakdownItemDto(
        DateUtil.toLocalDateTime(timeOffAdjustmentPojo.getCreatedAt()),
        dateMessage,
        timeOffAdjustmentPojo.getComment(),
        timeOffAdjustmentPojo.getAmount(),
        BreakDownType.TIME_OFF_ADJUSTMENT
    );
  }

  public static String dateFormatConvert(final LocalDateTime date) {
    return date.getYear() == LocalDate.now().getYear()
            ? DateUtil.formatDateTo(date, "MMM d") : DateUtil.formatDateTo(date, "MMM d, YYYY");
  }
}
