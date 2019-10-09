package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.user.entity.User;
import shamu.company.utils.DateUtil;

public class TimeOffAccrualService {

  static final String TIME_OFF_ACCRUED = "Time Off Accrued";

  List<Timestamp> getValidAccrualScheduleStartAndEndDate(final LocalDateTime userJoinDate,
      final TimeOffPolicyAccrualSchedule accrualSchedule) {

    LocalDateTime startDateTime = userJoinDate;

    final Long frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();
    if (AccrualFrequencyType.FREQUENCY_TYPE_THREE.equalsTo(frequencyId)) {
      final int startDayDelay = accrualSchedule.getDaysBeforeAccrualStarts() != null
          ? accrualSchedule.getDaysBeforeAccrualStarts() : 0;
      startDateTime = userJoinDate.plusDays(startDayDelay);
    }

    final LocalDateTime accrualScheduleEffectTime =
        DateUtil.toLocalDateTime(accrualSchedule.getCreatedAt());

    final LocalDateTime effectStartTime = startDateTime.isBefore(accrualScheduleEffectTime)
        ? accrualScheduleEffectTime
        : startDateTime;

    final List<Timestamp> dates = new ArrayList<>();
    dates.add(Timestamp.valueOf(effectStartTime));
    dates.add(accrualSchedule.getExpiredAt());
    return dates;
  }

  List<Timestamp> getValidMilestoneStartAndEndDate(final LocalDateTime userJoinDate,
      final AccrualScheduleMilestone accrualScheduleMilestone) {

    final Integer anniversaryYear = accrualScheduleMilestone.getAnniversaryYear();
    final LocalDateTime milestoneStartDate = userJoinDate.plusYears(anniversaryYear);

    final LocalDateTime accrualMilestoneEffectTime =
        DateUtil.toLocalDateTime(accrualScheduleMilestone.getCreatedAt());
    final LocalDateTime milestoneValidStart =
        milestoneStartDate.isBefore(accrualMilestoneEffectTime)
            ? accrualMilestoneEffectTime
            : milestoneStartDate;

    final List<Timestamp> dates = new ArrayList<>();
    dates.add(Timestamp.valueOf(milestoneValidStart));
    dates.add(accrualScheduleMilestone.getExpiredAt());
    return dates;
  }

  List<AccrualScheduleMilestone> trimTimeOffPolicyScheduleMilestones(
      final List<AccrualScheduleMilestone> accrualScheduleMilestoneList, final User user,
      final TimeOffPolicyAccrualSchedule accrualSchedule) {

    final List<AccrualScheduleMilestone> trimmedAccrualMilestones = new ArrayList<>();
    for (final AccrualScheduleMilestone accrualScheduleMilestone : accrualScheduleMilestoneList) {

      if (accrualScheduleMilestone.getExpiredAt() == null) {
        trimmedAccrualMilestones.add(accrualScheduleMilestone);
        continue;
      }

      final LocalDateTime userEnrollDateTime = DateUtil.toLocalDateTime(user.getCreatedAt());

      final Long frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();
      if (invalidByStartDateAndEndDate(accrualScheduleMilestone.getCreatedAt(),
          accrualScheduleMilestone.getExpiredAt(), userEnrollDateTime, frequencyId)) {
        continue;
      }

      trimmedAccrualMilestones.add(accrualScheduleMilestone);
    }
    return trimmedAccrualMilestones;
  }

  void populateRemainingAdjustment(
      final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final List<TimeOffBreakdownItemDto> balanceAdjustmentList,
      final TimeOffBalancePojo balancePojo) {

    balanceAdjustmentList.forEach(balanceAdjustment -> {

      Integer newAppliedAccumulation =
          balancePojo.getAppliedAccumulation() + balanceAdjustment.getAmount();
      balancePojo.setAppliedAccumulation(newAppliedAccumulation);

      if (balancePojo.reachMaxBalance(true)) {
        newAppliedAccumulation = balancePojo.getMaxBalance() - balancePojo.getBalance();
        balancePojo.setAppliedAccumulation(newAppliedAccumulation);
      }
      final Integer newBalance = balancePojo.getBalance() + balancePojo.getAppliedAccumulation();
      balanceAdjustment.setBalance(newBalance);

      resultTimeOffBreakdownItemList.add(balanceAdjustment);
    });

    balancePojo.calculateLatestBalance();
  }

  void populateBreakdownAdjustmentBefore(
      final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final LocalDateTime untilDateTime, final List<TimeOffBreakdownItemDto> balanceAdjustmentList,
      final TimeOffBalancePojo calculatePojo) {
    final Iterator<TimeOffBreakdownItemDto> adjustmentIterator = balanceAdjustmentList.iterator();

    while (adjustmentIterator.hasNext()) {
      final TimeOffBreakdownItemDto adjustment = adjustmentIterator.next();
      if (adjustment.getDate().isBefore(untilDateTime)) {

        setNewAppliedAccumulation(adjustment.getAmount(), calculatePojo);
        resetAccumulationIfExceed(calculatePojo);

        adjustment.setBalance(calculatePojo.getBalance() + calculatePojo.getAppliedAccumulation());
        resultTimeOffBreakdownItemList.add(adjustment);
        adjustmentIterator.remove();
      }
    }
  }

  void tuneBalanceWithCarryOverLimit(final TimeOffBreakdownItemDto lastTimeOffBreakdown,
      final TimeOffBalancePojo calculatePojo) {
    if (lastTimeOffBreakdown == null) {
      return;
    }

    if (calculatePojo.getAppliedAccumulation() == null) {
      calculatePojo.setAppliedAccumulation(0);
    }

    final Integer carryoverLimit = calculatePojo.getCarryOverLimit();
    final Integer maxBalance = calculatePojo.getMaxBalance();
    final Integer balance = calculatePojo.getBalance();

    final Integer previousAmount = calculatePojo.getAppliedAccumulation();

    if (carryoverLimit != null && previousAmount > carryoverLimit) {
      calculatePojo.setAppliedAccumulation(carryoverLimit);
    }

    if (calculatePojo.reachMaxBalance(true)) {
      final int newAppliedAccumulation = maxBalance - balance;
      calculatePojo.setAppliedAccumulation(newAppliedAccumulation);
    }
  }


  private void setNewAppliedAccumulation(final Integer adjustment,
      final TimeOffBalancePojo calculatePojo) {
    if (calculatePojo.getAppliedAccumulation() == null) {
      calculatePojo.setAppliedAccumulation(adjustment);
    } else {
      final int newAccumulation = calculatePojo.getAppliedAccumulation() + adjustment;
      calculatePojo.setAppliedAccumulation(newAccumulation);
    }
  }

  private void resetAccumulationIfExceed(final TimeOffBalancePojo calculatePojo) {
    if (calculatePojo.reachMaxBalance(true)) {
      final int newAccumulation = calculatePojo.getMaxBalance() - calculatePojo.getBalance();
      calculatePojo.setAppliedAccumulation(newAccumulation);
    }
  }

  private static boolean isSameYear(final LocalDateTime createDateTime,
      final LocalDateTime expireDateTime) {
    final LocalDate endDateOfYear = LocalDate.of(expireDateTime.getYear(), Month.DECEMBER,
        Month.DECEMBER.length(Year.isLeap(expireDateTime.getYear())));
    final LocalDateTime maxTimeInYear = LocalDateTime.of(endDateOfYear, LocalTime.MAX);

    return (createDateTime.getYear() == expireDateTime.getYear()
        && (expireDateTime.isBefore(maxTimeInYear)));
  }

  private static boolean isSameAnniversaryYear(final LocalDateTime userEnrollDateTime,
      final LocalDateTime createDateTime, final LocalDateTime expireDateTime) {
    final Duration startDuration = Duration.between(userEnrollDateTime, createDateTime);
    final Duration expireDuration = Duration.between(userEnrollDateTime, expireDateTime);

    final Long millisOfOneYear = 365L * 1000L * 60L * 60L * 24L;
    return expireDuration.toMillis() / millisOfOneYear
        == startDuration.toMillis() / millisOfOneYear;
  }

  private static boolean isSameMonth(final LocalDateTime createDateTime,
      final LocalDateTime expireDateTime) {

    final LocalDateTime maxTimeInMonth = LocalDateTime
        .of(LocalDate.of(expireDateTime.getYear(), expireDateTime.getMonthValue(),
            Month.of(expireDateTime.getMonthValue())
                .length(Year.isLeap(expireDateTime.getYear()))),
            LocalTime.MAX);

    final boolean inSameMonth = (createDateTime.getMonth().equals(expireDateTime.getMonth())
        && expireDateTime.isBefore(maxTimeInMonth));

    return isSameYear(createDateTime, expireDateTime) && inSameMonth;
  }

  static boolean invalidByStartDateAndEndDate(final Timestamp startDate, final Timestamp endDate,
      final LocalDateTime userEnrollDateTime, final Long frequencyTypeId) {

    final LocalDateTime createDateTime = DateUtil.toLocalDateTime(startDate);
    final LocalDateTime expireDateTime = DateUtil.toLocalDateTime(endDate);

    // case when expired before user enroll
    if (userEnrollDateTime.compareTo(expireDateTime) > 0) {
      return false;
    }

    if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE
        .equalsTo(frequencyTypeId)) {

      return isSameYear(createDateTime, expireDateTime);
    } else if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO
        .equalsTo(frequencyTypeId)) {

      // case when expired before first anniversary
      if (userEnrollDateTime.compareTo(createDateTime) > 0
          && userEnrollDateTime.plusDays(365).compareTo(expireDateTime) < 0) {
        return false;
      }
      return isSameAnniversaryYear(userEnrollDateTime, createDateTime, expireDateTime);
    } else if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_THREE
        .equalsTo(frequencyTypeId)) {

      return isSameMonth(createDateTime, expireDateTime);
    }

    return false;
  }
}
