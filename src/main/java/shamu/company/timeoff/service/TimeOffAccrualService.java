package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto.BreakDownType;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.utils.DateUtil;

public abstract class TimeOffAccrualService {

  private static final String TIME_OFF_ACCRUED = "Time Off Accrued";

  private static final String STARTING_BREAKDOWN_DETAIL = "Starting Balance";

  private static final String CARRYOVER_LIMIT_DETAIL = "Carryover Limit";

  private static final String MAX_BALANCE_DETAIL = "Max Balance";

  TimeOffBreakdownDto getTimeOffBreakdown(final TimeOffBreakdownYearDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {
    final TimeOffBreakdownDto timeOffBreakdownDto = getTimeOffBreakdownInternal(startingBreakdown,
        calculatePojo);
    postProcessOfTimeOffBreakdown(timeOffBreakdownDto, calculatePojo);

    return timeOffBreakdownDto;
  }

  abstract TimeOffBreakdownDto getTimeOffBreakdownInternal(
      final TimeOffBreakdownYearDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo);

  private void postProcessOfTimeOffBreakdown(final TimeOffBreakdownDto timeOffBreakdownDto,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    final List<TimeOffBreakdownItemDto> timeOffBreakdownItemList = timeOffBreakdownDto.getList();

    final List<TimeOffBreakdownItemDto> newTimeOffBreakdownItemList = timeOffBreakdownItemList
        .stream()
        .filter((timeOffBreakdownItemDto -> !timeOffBreakdownItemDto.getDate()
            .isAfter(calculatePojo.getUntilDate())))
        .sorted(Comparator.comparing(TimeOffBreakdownItemDto::getDate))
        .collect(Collectors.toList());
    timeOffBreakdownDto.setList(newTimeOffBreakdownItemList);
    timeOffBreakdownDto.resetBalance();
    timeOffBreakdownDto.setShowBalance(true);
  }

  private static boolean isSameYear(final LocalDateTime createDateTime,
      final LocalDateTime expireDateTime) {

    return createDateTime.getYear() == expireDateTime.getYear();
  }

  private static boolean isSameAnniversaryYear(final LocalDateTime userJoinPolicyDateTime,
      final LocalDateTime createDateTime, final LocalDateTime expireDateTime) {
    final Duration startDuration = Duration.between(userJoinPolicyDateTime, createDateTime);
    final Duration expireDuration = Duration.between(userJoinPolicyDateTime, expireDateTime);

    final Long millisOfOneYear = 365L * 1000L * 60L * 60L * 24L;
    return expireDuration.toMillis() / millisOfOneYear
        == startDuration.toMillis() / millisOfOneYear;
  }

  private static boolean isSameMonth(final LocalDateTime createDateTime,
      final LocalDateTime expireDateTime) {

    return isSameYear(createDateTime, expireDateTime)
        && createDateTime.getMonthValue() == expireDateTime.getMonthValue();
  }

  static boolean invalidByStartDateAndEndDate(final Timestamp startDate, final Timestamp endDate,
      final LocalDateTime userJoinPolicyDateTime, final String frequencyType) {

    final LocalDateTime createDateTime = DateUtil.toLocalDateTime(startDate);
    final LocalDateTime expireDateTime = DateUtil.toLocalDateTime(endDate);

    // case when expired before user joined
    if (userJoinPolicyDateTime.compareTo(expireDateTime) > 0) {
      return false;
    }

    if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE
        .equalsTo(frequencyType)) {

      return isSameYear(createDateTime, expireDateTime);
    } else if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO
        .equalsTo(frequencyType)) {

      // in valid time window, return false
      if (userJoinPolicyDateTime.compareTo(createDateTime) > 0
          && userJoinPolicyDateTime.plusDays(365).compareTo(expireDateTime) < 0) {
        return false;
      }
      return isSameAnniversaryYear(userJoinPolicyDateTime, createDateTime, expireDateTime);
    } else if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_THREE
        .equalsTo(frequencyType)) {

      return isSameMonth(createDateTime, expireDateTime);
    }

    return false;
  }

  static LocalDate getScheduleStartBaseTime(final LocalDate hireDate,
      final LocalDate userJoinDate, final TimeOffPolicyAccrualSchedule accrualSchedule) {
    LocalDate delayedHireDate = hireDate;

    final String frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();
    if (AccrualFrequencyType.FREQUENCY_TYPE_THREE.equalsTo(frequencyId)) {
      final int startDayDelay = accrualSchedule.getDaysBeforeAccrualStarts() != null
          ? accrualSchedule.getDaysBeforeAccrualStarts() : 0;
      delayedHireDate = delayedHireDate.plusDays(startDayDelay);
    }

    return delayedHireDate != null && userJoinDate.isBefore(delayedHireDate)
        ? delayedHireDate : userJoinDate;
  }

  static List<LocalDate> getValidScheduleOrMilestonePeriod(final LocalDate scheduleBaseTime,
      final Timestamp startTime, final Timestamp endTime) {

    final LocalDate startDate = DateUtil.fromTimestamp(startTime);

    final LocalDate effectStart = scheduleBaseTime.isAfter(startDate)
        ? scheduleBaseTime
        : startDate;

    final List<LocalDate> dates = new ArrayList<>();
    dates.add(effectStart);

    LocalDate scheduleEndTime = null;
    if (endTime != null) {
      scheduleEndTime = DateUtil.fromTimestamp(endTime);
    }
    dates.add(scheduleEndTime);
    return dates;
  }

  static List<AccrualScheduleMilestone> trimTimeOffPolicyScheduleMilestones(
      final List<AccrualScheduleMilestone> accrualScheduleMilestoneList,
      final TimeOffPolicyUser policyUser,
      final TimeOffPolicyAccrualSchedule accrualSchedule) {

    final List<AccrualScheduleMilestone> trimmedAccrualMilestones = new ArrayList<>();
    for (final AccrualScheduleMilestone accrualScheduleMilestone : accrualScheduleMilestoneList) {

      if (accrualScheduleMilestone.getExpiredAt() == null) {
        trimmedAccrualMilestones.add(accrualScheduleMilestone);
        continue;
      }

      final LocalDateTime userJoinPolicyDateTime =
          DateUtil.toLocalDateTime(policyUser.getCreatedAt());

      final String frequencyType = accrualSchedule.getTimeOffAccrualFrequency().getName();
      if (invalidByStartDateAndEndDate(accrualScheduleMilestone.getCreatedAt(),
          accrualScheduleMilestone.getExpiredAt(), userJoinPolicyDateTime, frequencyType)) {
        continue;
      }

      trimmedAccrualMilestones.add(accrualScheduleMilestone);
    }
    return trimmedAccrualMilestones;
  }

  static void populateRemainingAdjustment(
      final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final List<TimeOffBreakdownItemDto> balanceAdjustmentList,
      final TimeOffBalancePojo balancePojo) {

    balanceAdjustmentList.forEach(balanceAdjustment -> {

      final Integer newBalance = balancePojo.getBalance() + balanceAdjustment.getAmount();
      balanceAdjustment.setBalance(newBalance);
      balancePojo.setBalance(newBalance);

      resultTimeOffBreakdownItemList.add(balanceAdjustment);

      // max balance
      populateBreakdownListFromMaxBalance(resultTimeOffBreakdownItemList,
          balanceAdjustment.getDate(), balancePojo);
    });
  }

  static void populateBreakdownAdjustmentBefore(
      final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final LocalDate untilDate, final List<TimeOffBreakdownItemDto> balanceAdjustmentList,
      final TimeOffBalancePojo balancePojo) {
    final Iterator<TimeOffBreakdownItemDto> adjustmentIterator = balanceAdjustmentList.iterator();

    while (adjustmentIterator.hasNext()) {
      final TimeOffBreakdownItemDto adjustment = adjustmentIterator.next();
      if (adjustment.getDate().isBefore(untilDate)) {

        final Integer newBalance = balancePojo.getBalance() + adjustment.getAmount();
        adjustment.setBalance(newBalance);
        balancePojo.setBalance(newBalance);

        resultTimeOffBreakdownItemList.add(adjustment);
        adjustmentIterator.remove();

        // max balance
        populateBreakdownListFromMaxBalance(resultTimeOffBreakdownItemList,
            adjustment.getDate(), balancePojo);
      }
    }
  }

  static void populateBreakdownListFromAccrualSchedule(
      final List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final LocalDate date, final Integer accrualHours,
      final TimeOffBalancePojo balancePojo) {

    final String dateMessage =
        TimeOffBreakdownItemDto.dateFormatConvert(date);

    balancePojo.setBalance(balancePojo.getBalance() + accrualHours);

    final String accrualDetail = resultTimeOffBreakdownItemList.isEmpty()
        ? STARTING_BREAKDOWN_DETAIL
        : TIME_OFF_ACCRUED;

    final TimeOffBreakdownItemDto timeOffBreakdownItemDto =
        TimeOffBreakdownItemDto.builder()
            .date(date)
            .dateMessage(dateMessage)
            .amount(accrualHours)
            .balance(balancePojo.getBalance())
            .detail(accrualDetail)
            .breakdownType(TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_ACCRUAL)
            .build();
    resultTimeOffBreakdownItemList.add(timeOffBreakdownItemDto);

    // max balance
    populateBreakdownListFromMaxBalance(resultTimeOffBreakdownItemList,
        date, balancePojo);
  }

  static void populateBreakdownListFromCarryoverLimit(
      final List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final LocalDate date,
      final TimeOffBalancePojo balancePojo) {

    if (balancePojo.getCarryOverLimit() == null
        || balancePojo.getBalance() <= balancePojo.getCarryOverLimit()) {
      return;
    }

    final int adjustment = balancePojo.getCarryOverLimit() - balancePojo.getBalance();
    balancePojo.setBalance(balancePojo.getCarryOverLimit());

    final String dateMessage =
        TimeOffBreakdownItemDto.dateFormatConvert(date);

    final TimeOffBreakdownItemDto timeOffBreakdownItemDto =
        TimeOffBreakdownItemDto.builder()
            .date(date)
            .dateMessage(dateMessage)
            .amount(adjustment)
            .balance(balancePojo.getBalance())
            .detail(CARRYOVER_LIMIT_DETAIL)
            .breakdownType(BreakDownType.CARRYOVER_LIMIT)
            .build();
    resultTimeOffBreakdownItemList.add(timeOffBreakdownItemDto);
  }

  static void populateBreakdownListFromMaxBalance(
      final List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final LocalDate date,
      final TimeOffBalancePojo balancePojo) {

    if (balancePojo.getMaxBalance() == null
        || balancePojo.getBalance() <= balancePojo.getMaxBalance()) {
      return;
    }

    final int adjustment = balancePojo.getMaxBalance() - balancePojo.getBalance();
    balancePojo.setBalance(balancePojo.getMaxBalance());

    final String dateMessage =
        TimeOffBreakdownItemDto.dateFormatConvert(date);

    final TimeOffBreakdownItemDto timeOffBreakdownItemDto =
        TimeOffBreakdownItemDto.builder()
            .date(date)
            .dateMessage(dateMessage)
            .amount(adjustment)
            .balance(balancePojo.getBalance())
            .detail(MAX_BALANCE_DETAIL)
            .breakdownType(BreakDownType.MAX_BALANCE)
            .build();
    resultTimeOffBreakdownItemList.add(timeOffBreakdownItemDto);
  }

  abstract boolean support(String frequencyType);
}
