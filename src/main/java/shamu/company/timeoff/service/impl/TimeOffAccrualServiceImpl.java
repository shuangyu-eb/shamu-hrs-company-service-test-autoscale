package shamu.company.timeoff.service.impl;

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

public class TimeOffAccrualServiceImpl {

  static final String TIME_OFF_ACCRUED = "Time Off Accrued";

  List<Timestamp> getValidAccrualScheduleStartAndEndDate(LocalDateTime userJoinDate,
      TimeOffPolicyAccrualSchedule accrualSchedule) {

    LocalDateTime startDateTime = userJoinDate;

    Long frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();
    if (AccrualFrequencyType.FREQUENCY_TYPE_THREE.equalsTo(frequencyId)) {
      int startDayDelay = accrualSchedule.getDaysBeforeAccrualStarts() != null
          ? accrualSchedule.getDaysBeforeAccrualStarts() : 0;
      startDateTime = userJoinDate.plusDays(startDayDelay);
    }
    
    LocalDateTime accrualScheduleEffectTime =
        DateUtil.toLocalDateTime(accrualSchedule.getCreatedAt());

    LocalDateTime effectStartTime = startDateTime.isBefore(accrualScheduleEffectTime)
        ? accrualScheduleEffectTime
        : startDateTime;

    List<Timestamp> dates = new ArrayList<>();
    dates.add(Timestamp.valueOf(effectStartTime));
    dates.add(accrualSchedule.getExpiredAt());
    return dates;
  }

  List<Timestamp> getValidMilestoneStartAndEndDate(LocalDateTime userJoinDate,
      AccrualScheduleMilestone accrualScheduleMilestone) {

    Integer anniversaryYear = accrualScheduleMilestone.getAnniversaryYear();
    LocalDateTime milestoneStartDate = userJoinDate.plusYears(anniversaryYear);

    LocalDateTime accrualMilestoneEffectTime =
        DateUtil.toLocalDateTime(accrualScheduleMilestone.getCreatedAt());
    LocalDateTime milestoneValidStart = milestoneStartDate.isBefore(accrualMilestoneEffectTime)
        ? accrualMilestoneEffectTime
        : milestoneStartDate;

    List<Timestamp> dates = new ArrayList<>();
    dates.add(Timestamp.valueOf(milestoneValidStart));
    dates.add(accrualScheduleMilestone.getExpiredAt());
    return dates;
  }

  List<AccrualScheduleMilestone> trimTimeOffPolicyScheduleMilestones(
      List<AccrualScheduleMilestone> accrualScheduleMilestoneList, User user,
      TimeOffPolicyAccrualSchedule accrualSchedule) {

    List<AccrualScheduleMilestone> trimmedAccrualMilestones = new ArrayList<>();
    for (AccrualScheduleMilestone accrualScheduleMilestone : accrualScheduleMilestoneList) {

      if (accrualScheduleMilestone.getExpiredAt() == null) {
        trimmedAccrualMilestones.add(accrualScheduleMilestone);
        continue;
      }

      LocalDateTime userEnrollDateTime = DateUtil.toLocalDateTime(user.getCreatedAt());

      Long frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();
      if (invalidByStartDateAndEndDate(accrualScheduleMilestone.getCreatedAt(),
          accrualScheduleMilestone.getExpiredAt(), userEnrollDateTime, frequencyId)) {
        continue;
      }

      trimmedAccrualMilestones.add(accrualScheduleMilestone);
    }
    return trimmedAccrualMilestones;
  }

  void populateRemainingAdjustment(
      LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      List<TimeOffBreakdownItemDto> balanceAdjustmentList, TimeOffBalancePojo balancePojo) {

    balanceAdjustmentList.forEach(balanceAdjustment -> {

      Integer newAppliedAccumulation =
          balancePojo.getAppliedAccumulation() + balanceAdjustment.getAmount();
      balancePojo.setAppliedAccumulation(newAppliedAccumulation);

      if (balancePojo.reachMaxBalance(true)) {
        newAppliedAccumulation = balancePojo.getMaxBalance() - balancePojo.getBalance();
        balancePojo.setAppliedAccumulation(newAppliedAccumulation);
      }
      Integer newBalance = balancePojo.getBalance() + balancePojo.getAppliedAccumulation();
      balanceAdjustment.setBalance(newBalance);

      resultTimeOffBreakdownItemList.add(balanceAdjustment);
    });

    balancePojo.calculateLatestBalance();
  }

  void populateBreakdownAdjustmentBefore(
      LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      LocalDateTime untilDateTime, List<TimeOffBreakdownItemDto> balanceAdjustmentList,
      TimeOffBalancePojo calculatePojo) {
    Iterator<TimeOffBreakdownItemDto> adjustmentIterator = balanceAdjustmentList.iterator();

    while (adjustmentIterator.hasNext()) {
      TimeOffBreakdownItemDto adjustment = adjustmentIterator.next();
      if (adjustment.getDate().isBefore(untilDateTime)) {

        setNewAppliedAccumulation(adjustment.getAmount(), calculatePojo);
        resetAccumulationIfExceed(calculatePojo);

        adjustment.setBalance(calculatePojo.getBalance() + calculatePojo.getAppliedAccumulation());
        resultTimeOffBreakdownItemList.add(adjustment);
        adjustmentIterator.remove();
      }
    }
  }

  void tuneBalanceWithCarryOverLimit(TimeOffBreakdownItemDto lastTimeOffBreakdown,
      TimeOffBalancePojo calculatePojo) {
    if (lastTimeOffBreakdown == null) {
      return;
    }

    if (calculatePojo.getAppliedAccumulation() == null) {
      calculatePojo.setAppliedAccumulation(0);
    }

    Integer carryoverLimit = calculatePojo.getCarryOverLimit();
    Integer maxBalance = calculatePojo.getMaxBalance();
    Integer balance = calculatePojo.getBalance();

    Integer previousAmount = calculatePojo.getAppliedAccumulation();

    if (carryoverLimit != null && previousAmount > carryoverLimit) {
      calculatePojo.setAppliedAccumulation(carryoverLimit);
    }

    if (calculatePojo.reachMaxBalance(true)) {
      int newAppliedAccumulation = maxBalance - balance;
      calculatePojo.setAppliedAccumulation(newAppliedAccumulation);
    }
  }


  private void setNewAppliedAccumulation(Integer adjustment, TimeOffBalancePojo calculatePojo) {
    if (calculatePojo.getAppliedAccumulation() == null) {
      calculatePojo.setAppliedAccumulation(adjustment);
    } else {
      int newAccumulation = calculatePojo.getAppliedAccumulation() + adjustment;
      calculatePojo.setAppliedAccumulation(newAccumulation);
    }
  }

  private void resetAccumulationIfExceed(TimeOffBalancePojo calculatePojo) {
    if (calculatePojo.reachMaxBalance(true)) {
      int newAccumulation = calculatePojo.getMaxBalance() - calculatePojo.getBalance();
      calculatePojo.setAppliedAccumulation(newAccumulation);
    }
  }

  private static boolean isSameYear(LocalDateTime createDateTime, LocalDateTime expireDateTime) {
    LocalDate endDateOfYear = LocalDate.of(expireDateTime.getYear(), Month.DECEMBER,
        Month.DECEMBER.length(Year.isLeap(expireDateTime.getYear())));
    LocalDateTime maxTimeInYear = LocalDateTime.of(endDateOfYear, LocalTime.MAX);

    return (createDateTime.getYear() == expireDateTime.getYear()
        && (expireDateTime.isBefore(maxTimeInYear)));
  }

  private static boolean isSameAnniversaryYear(LocalDateTime userEnrollDateTime,
      LocalDateTime createDateTime, LocalDateTime expireDateTime) {
    Duration startDuration = Duration.between(userEnrollDateTime, createDateTime);
    Duration expireDuration = Duration.between(userEnrollDateTime, expireDateTime);

    Long millisOfOneYear = 365L * 1000L * 60L * 60L * 24L;
    return expireDuration.toMillis() / millisOfOneYear
        == startDuration.toMillis() / millisOfOneYear;
  }

  private static boolean isSameMonth(LocalDateTime createDateTime, LocalDateTime expireDateTime) {

    LocalDateTime maxTimeInMonth = LocalDateTime
        .of(LocalDate.of(expireDateTime.getYear(), expireDateTime.getMonthValue(),
            Month.of(expireDateTime.getMonthValue())
                .length(Year.isLeap(expireDateTime.getYear()))),
            LocalTime.MAX);

    boolean inSameMonth = (createDateTime.getMonth().equals(expireDateTime.getMonth())
        && expireDateTime.isBefore(maxTimeInMonth));

    return isSameYear(createDateTime, expireDateTime) && inSameMonth;
  }

  static boolean invalidByStartDateAndEndDate(Timestamp startDate, Timestamp endDate,
      LocalDateTime userEnrollDateTime, Long frequencyTypeId) {

    LocalDateTime createDateTime = DateUtil.toLocalDateTime(startDate);
    LocalDateTime expireDateTime = DateUtil.toLocalDateTime(endDate);

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
