package shamu.company.timeoff.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffAdjustmentPojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.utils.DateUtil;

@Data
@NoArgsConstructor
@Builder
public class TimeOffBreakdownItemDto {

  private static final String STARTING_BREAKDOWN_DETAIL = "Starting Balance";
  private LocalDate date;
  private String dateMessage;
  private String detail;
  private Integer amount;
  private Integer balance;
  @JsonIgnore private BreakDownType breakdownType;

  public TimeOffBreakdownItemDto(
      final LocalDate date,
      final String dateMessage,
      final String detail,
      final Integer amount,
      final Integer balance,
      final BreakDownType breakdownType) {
    this.date = date;
    this.dateMessage = dateMessage;
    this.detail = detail;
    this.amount = amount;
    this.balance = balance;
    this.breakdownType = breakdownType;
  }

  public TimeOffBreakdownItemDto(
      final LocalDate date,
      final String dateMessage,
      final String detail,
      final Integer amount,
      final BreakDownType breakdownType) {
    this.date = date;
    this.dateMessage = dateMessage;
    this.detail = detail;
    this.amount = amount;
    this.breakdownType = breakdownType;
  }

  @JsonIgnore
  public static TimeOffBreakdownYearDto fromTimeOffPolicyUser(
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    final TimeOffPolicyUser timeOffPolicyUser = calculatePojo.getPolicyUser();
    Integer startingBalance = timeOffPolicyUser.getInitialBalance();
    if (startingBalance == null) {
      startingBalance = 0;
    }

    final TimeOffBreakdownYearDto timeOffBreakdownYearDto =
        TimeOffBreakdownYearDto.builder()
            .accrualHours(startingBalance)
            .date(DateUtil.fromTimestamp(timeOffPolicyUser.getCreatedAt()))
            .build();

    calculatePojo.getTrimmedScheduleList().stream()
        .filter(
            timeOffSchedule ->
                DateUtil.fromTimestamp(timeOffSchedule.getCreatedAt()).getYear()
                    == timeOffBreakdownYearDto.getDate().getYear())
        .forEach(
            (accrualSchedule -> {
              timeOffBreakdownYearDto.setMaxBalance(accrualSchedule.getMaxBalance());
              timeOffBreakdownYearDto.setCarryoverLimit(accrualSchedule.getCarryoverLimit());
            }));

    return timeOffBreakdownYearDto;
  }

  @JsonIgnore
  public static TimeOffBreakdownItemDto fromTimeOffAdjustment(
      final TimeOffAdjustmentPojo timeOffAdjustmentPojo) {

    final LocalDate createTime = DateUtil.toLocalDate(timeOffAdjustmentPojo.getCreatedAt());

    final String dateMessage = dateFormatConvert(createTime);

    return new TimeOffBreakdownItemDto(
        createTime,
        dateMessage,
        timeOffAdjustmentPojo.getComment(),
        timeOffAdjustmentPojo.getAmount(),
        BreakDownType.TIME_OFF_ADJUSTMENT);
  }

  public static String dateFormatConvert(final LocalDate date) {
    return date.getYear() == LocalDate.now().getYear()
        ? DateUtil.formatDateTo(date, "MMM d")
        : DateUtil.formatDateTo(date, "MMM d, YYYY");
  }

  public enum BreakDownType {
    TIME_OFF_ACCRUAL,
    TIME_OFF_ADJUSTMENT,
    TIME_OFF_REQUEST,
    CARRYOVER_LIMIT,
    MAX_BALANCE,
  }
}
