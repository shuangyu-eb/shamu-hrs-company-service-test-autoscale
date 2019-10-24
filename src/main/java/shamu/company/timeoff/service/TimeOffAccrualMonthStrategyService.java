package shamu.company.timeoff.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownMonthDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffAccrualMonthStrategyService extends TimeOffAccrualService {

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Autowired
  public TimeOffAccrualMonthStrategyService(
      final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository) {
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
  }

  TimeOffBreakdownDto getTimeOffBreakdown(
      final TimeOffBreakdownItemDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList =
        getAccrualDataByMonth(calculatePojo);
    timeOffBreakdownMonthDtoList = addMissingMonthDto(timeOffBreakdownMonthDtoList);

    // calculate the month which dividing day reside
    // refer to https://tardisone.atlassian.net/browse/SH-689?focusedCommentId=28173&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-28173
    resetDividingMonths(timeOffBreakdownMonthDtoList, startingBreakdown.getDate());

    timeOffBreakdownMonthDtoList.forEach(timeOffBreakdownMonthDto -> {
      timeOffBreakdownMonthDto.setDate(timeOffBreakdownMonthDto.getDate().plusMonths(1));
    });

    return getFinalMonthBreakdown(timeOffBreakdownMonthDtoList,
        startingBreakdown, calculatePojo.getBalanceAdjustment());
  }

  private void resetDividingMonths(List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList,
      LocalDate dividingDay) {
    LocalDate[] currentDividingDay = new LocalDate[]{LocalDate.from(dividingDay)};
    currentDividingDay[0] = currentDividingDay[0].plusMonths(12);

    final TimeOffBreakdownMonthDto[] currentMonthDto = {null};
    TimeOffBreakdownMonthDto[] previousMonthDto = {null};

    timeOffBreakdownMonthDtoList.forEach(timeOffBreakdownMonthDto -> {

      previousMonthDto[0] = currentMonthDto[0];
      currentMonthDto[0] = timeOffBreakdownMonthDto;

      if (timeOffBreakdownMonthDto.getDate().getYear() != dividingDay.getYear()
          && timeOffBreakdownMonthDto.getDate().getMonthValue()
          == dividingDay.getMonthValue()) {
        resetDividingMonth(currentMonthDto[0], previousMonthDto[0], dividingDay);
      }
    });
  }

  private void resetDividingMonth(TimeOffBreakdownMonthDto dividingMonth,
      TimeOffBreakdownMonthDto previousMonth, LocalDate dividingDay) {

    int dayOfMonth = dividingDay.getDayOfMonth();
    int totalDays = dividingMonth.getDate().getMonth().length(
        Year.isLeap(dividingMonth.getDate().getYear())
    );

    Double newBalance = previousMonth.getAccrualHours() * ((double)dayOfMonth / totalDays)
        + dividingMonth.getAccrualHours() * (1d - (double)dayOfMonth / totalDays);

    dividingMonth.setAccrualHours((int) Math.round(newBalance));
    previousMonth.setLastMonthOfPreviousAnniversaryYear(true);
  }

  private List<TimeOffBreakdownMonthDto> getAccrualDataByMonth(
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    final LocalDate userHiredDate = DateUtil
        .fromTimestamp(calculatePojo.getPolicyUser().getUser().getCreatedAt());
    final LocalDateTime userJoinPolicyDate = DateUtil
        .toLocalDateTime(calculatePojo.getPolicyUser().getCreatedAt());

    final TreeMap<Integer, TreeMap<Integer, TimeOffBreakdownMonthDto>> accrualDate
        = new TreeMap<>();

    calculatePojo.getTrimmedScheduleList().forEach(accrualSchedule -> {

      LocalDate scheduleBaseTime =
          super.getScheduleStartBaseTime(userHiredDate,
              userJoinPolicyDate.toLocalDate(), accrualSchedule);
      final List<LocalDate> validStartAndEndDate =
          super.getValidScheduleOrMilestonePeriod(scheduleBaseTime,
          accrualSchedule.getCreatedAt(), accrualSchedule.getExpiredAt());

      final List<LocalDate> validPeriod = getValidMonthPeriod(
          validStartAndEndDate.get(0),
          validStartAndEndDate.get(1),
          calculatePojo.getUntilDate()
      );

      addToResultMonthMap(accrualDate, validPeriod, accrualSchedule);

      List<AccrualScheduleMilestone> accrualScheduleMilestoneList =
          accrualScheduleMilestoneRepository
              .findByTimeOffPolicyAccrualScheduleId(accrualSchedule.getId());
      accrualScheduleMilestoneList = trimTimeOffPolicyScheduleMilestones(
          accrualScheduleMilestoneList,
          calculatePojo.getPolicyUser(), accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator
          .comparing(AccrualScheduleMilestone::getCreatedAt, Comparator.reverseOrder()));

      accrualScheduleMilestoneList.forEach(accrualScheduleMilestone -> {
        LocalDateTime milestoneStartTime = userJoinPolicyDate
            .plusYears(accrualScheduleMilestone.getAnniversaryYear());
        final List<LocalDate> validMilestoneStartAndEndDate =
            super.getValidScheduleOrMilestonePeriod(milestoneStartTime.toLocalDate(),
                accrualScheduleMilestone.getCreatedAt(), accrualScheduleMilestone.getExpiredAt());

        final List<LocalDate> validMilestonePeriod = getValidMonthPeriod(
            validMilestoneStartAndEndDate.get(0), validMilestoneStartAndEndDate.get(1),
            calculatePojo.getUntilDate());

        final TimeOffPolicyAccrualSchedule tempAccrualSchedule = new TimeOffPolicyAccrualSchedule();
        BeanUtils.copyProperties(accrualScheduleMilestone, tempAccrualSchedule);
        addToResultMonthMap(accrualDate, validMilestonePeriod, tempAccrualSchedule);
      });
    });

    final List<TimeOffBreakdownMonthDto> monthList = new ArrayList<>();

    accrualDate.values().forEach(monthMap -> monthList.addAll(monthMap.values()));

    // recalculate first month's time off
    if (!monthList.isEmpty()) {
      final TimeOffBreakdownMonthDto firstMonth = monthList.get(0);
      final LocalDate startDate = firstMonth.getDate();
      final int dayOfMonth = startDate.getDayOfMonth();
      final int totalDays = Month.of(startDate.getMonthValue())
          .length(Year.isLeap(startDate.getYear()));
      final Double newTimeOffTime = (double) Math
          .round(firstMonth.getAccrualHours() * ((totalDays - dayOfMonth) / (double) totalDays));
      firstMonth.setAccrualHours(newTimeOffTime.intValue());
    }

    return monthList;
  }

  private List<LocalDate> getValidMonthPeriod(final LocalDate startDate,
      final LocalDate endDate,
      final LocalDate selectedDate) {

    LocalDate currentMonthDate = startDate.withDayOfMonth(1);

    LocalDate endDateTime = endDate == null ? selectedDate
        : endDate;
    endDateTime = endDateTime.withDayOfMonth(1);

    final List<LocalDate> validPeriod = new ArrayList<>();

    while (!currentMonthDate.isAfter(endDateTime)) {
      validPeriod.add(currentMonthDate);
      currentMonthDate = currentMonthDate.plusMonths(1);
    }
    return validPeriod;
  }

  private void addToResultMonthMap(
      final TreeMap<Integer, TreeMap<Integer, TimeOffBreakdownMonthDto>> accrualDate,
      final List<LocalDate> validPeriodList,
      final TimeOffPolicyAccrualSchedule accrualSchedule) {
    for (final LocalDate currentTime : validPeriodList) {

      final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto = transferToMonthDto(accrualSchedule,
          currentTime, accrualSchedule.getAccrualHours() / 12);

      final int currentYear = timeOffBreakdownMonthDto.getDate().getYear();
      final int currentMonth = timeOffBreakdownMonthDto.getDate().getMonthValue();

      TreeMap<Integer, TimeOffBreakdownMonthDto> monthMap = accrualDate.get(currentYear);
      if (monthMap == null) {
        monthMap = new TreeMap<>();
        monthMap.put(currentMonth, timeOffBreakdownMonthDto);
        accrualDate.put(currentYear, monthMap);
      } else {
        monthMap.put(currentMonth, timeOffBreakdownMonthDto);
      }
    }
  }

  private <T> TimeOffBreakdownMonthDto transferToMonthDto(final T t,
      final LocalDate currentTime,
      final Integer monthAccrualHours) {
    final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto = new TimeOffBreakdownMonthDto(
        currentTime,
        monthAccrualHours
    );

    TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    timeOffBreakdownYearDto.setYear(currentTime.getYear());
    BeanUtils.copyProperties(t, timeOffBreakdownYearDto);
    timeOffBreakdownMonthDto.setYearData(timeOffBreakdownYearDto);
    return timeOffBreakdownMonthDto;
  }

  private List<TimeOffBreakdownMonthDto> addMissingMonthDto(
      final List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtos) {

    final LinkedList<TimeOffBreakdownMonthDto> newTimeOffBreakdownList = new LinkedList<>();

    LocalDate previousDate = null;
    for (final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto : timeOffBreakdownMonthDtos) {

      TimeOffBreakdownMonthDto previousMonthDto;
      while (previousDate != null && (previousDate = previousDate.plusMonths(1))
          .isBefore(timeOffBreakdownMonthDto.getDate())) {

        final TimeOffBreakdownMonthDto newMonthBreakdown = new TimeOffBreakdownMonthDto();
        previousMonthDto = newTimeOffBreakdownList.peekLast();
        BeanUtils.copyProperties(previousMonthDto, newMonthBreakdown);
        newMonthBreakdown.setDate(previousDate);

        newTimeOffBreakdownList.add(newMonthBreakdown);
      }

      newTimeOffBreakdownList.add(timeOffBreakdownMonthDto);
      previousDate = timeOffBreakdownMonthDto.getDate();
    }

    return newTimeOffBreakdownList;
  }

  private TimeOffBreakdownDto getFinalMonthBreakdown(
      final List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList,
      final TimeOffBreakdownItemDto startingBreakdown,
      final List<TimeOffBreakdownItemDto> balanceAdjustment) {

    final TimeOffBalancePojo balancePojo = new TimeOffBalancePojo(startingBreakdown.getBalance());

    TimeOffBreakdownYearDto timeOffBreakdownYearDto = null;
    final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    resultTimeOffBreakdownItemList.add(startingBreakdown);

    for (final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto : timeOffBreakdownMonthDtoList) {

      final LocalDate firstDateOfTheMonth = timeOffBreakdownMonthDto.getDate().withDayOfMonth(1);
      // Adjustment
      super.populateBreakdownAdjustmentBefore(resultTimeOffBreakdownItemList,
          firstDateOfTheMonth, balanceAdjustment, balancePojo);

      // carryover
      super.populateBreakdownListFromCarryoverLimit(resultTimeOffBreakdownItemList,
          firstDateOfTheMonth, balancePojo);

      // accrual
      super.populateBreakdownListFromAccrualSchedule(resultTimeOffBreakdownItemList,
          firstDateOfTheMonth, timeOffBreakdownMonthDto.getAccrualHours(), balancePojo);

      // max balance
      super.populateBreakdownListFromMaxBalance(resultTimeOffBreakdownItemList,
          firstDateOfTheMonth, balancePojo);

      timeOffBreakdownYearDto = timeOffBreakdownMonthDto.getLastMonthOfPreviousAnniversaryYear()
          ? timeOffBreakdownMonthDto.getYearData()
          : null;

      if (timeOffBreakdownYearDto != null) {
        balancePojo.setMaxBalance(timeOffBreakdownYearDto.getMaxBalance());

        balancePojo.setCarryOverLimit(timeOffBreakdownYearDto.getCarryoverLimit());
      } else {
        balancePojo.setCarryOverLimit(null);
      }
    }
    super.populateRemainingAdjustment(resultTimeOffBreakdownItemList,
        balanceAdjustment, balancePojo);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }
}
