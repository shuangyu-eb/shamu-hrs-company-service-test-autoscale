package shamu.company.timeoff.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import shamu.company.common.entity.BaseEntity;
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
public class TimeOffAccrualMonthStrategyServiceImpl extends TimeOffAccrualServiceImpl {

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Autowired
  public TimeOffAccrualMonthStrategyServiceImpl(
      AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository) {
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(TimeOffBreakdownItemDto startingBreakdown,
      TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList =
            getAccrualDataByMonth(calculatePojo);
    timeOffBreakdownMonthDtoList = addMissingMonthDto(timeOffBreakdownMonthDtoList);

    timeOffBreakdownMonthDtoList.forEach(timeOffBreakdownMonthDto -> {
      timeOffBreakdownMonthDto.setDate(timeOffBreakdownMonthDto.getDate().plusMonths(1));
    });

    return getFinalMonthBreakdown(timeOffBreakdownMonthDtoList,
        startingBreakdown, calculatePojo.getBalanceAdjustment());
  }

  private List<TimeOffBreakdownMonthDto> getAccrualDataByMonth(
          TimeOffBreakdownCalculatePojo calculatePojo) {

    LocalDateTime userJoinDateTime = DateUtil.toLocalDateTime(
            calculatePojo.getPolicyUser().getUser().getCreatedAt());
    TreeMap<Integer, TreeMap<Integer, TimeOffBreakdownMonthDto>> accrualDate = new TreeMap<>();

    calculatePojo.getTrimmedScheduleList().forEach(accrualSchedule -> {
      List<Timestamp> validStartAndEndDate =
          getValidAccrualScheduleStartAndEndDate(userJoinDateTime, accrualSchedule);

      List<LocalDateTime> validPeriod = getValidMonthPeriod(
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
              calculatePojo.getPolicyUser().getUser(), accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator.comparing(BaseEntity::getCreatedAt));

      accrualScheduleMilestoneList.forEach(accrualScheduleMilestone -> {

        List<Timestamp> validMilestoneStartAndEndDate =
            getValidMilestoneStartAndEndDate(userJoinDateTime, accrualScheduleMilestone);
        List<LocalDateTime> validMilestonePeriod = getValidMonthPeriod(
            validMilestoneStartAndEndDate.get(0), validMilestoneStartAndEndDate.get(1),
                calculatePojo.getUntilDate());

        TimeOffPolicyAccrualSchedule tempAccrualSchedule = new TimeOffPolicyAccrualSchedule();
        BeanUtils.copyProperties(accrualScheduleMilestone, tempAccrualSchedule);
        addToResultMonthMap(accrualDate, validMilestonePeriod, tempAccrualSchedule);
      });
    });

    List<TimeOffBreakdownMonthDto> monthList = new ArrayList<>();

    accrualDate.values().forEach(monthMap -> monthList.addAll(monthMap.values()));

    // recalculate first month's time off
    if (!monthList.isEmpty()) {
      TimeOffBreakdownMonthDto firstMonth = monthList.get(0);
      LocalDateTime startDate = firstMonth.getDate();
      int dayOfMonth = startDate.getDayOfMonth();
      int totalDays = Month.of(startDate.getMonthValue()).length(Year.isLeap(startDate.getYear()));
      Double newTimeOffTime = (double) Math
          .round(firstMonth.getAccrualHours() * ((totalDays - dayOfMonth) / (double) totalDays));
      firstMonth.setAccrualHours(newTimeOffTime.intValue());
    }

    // reset day value to be the first day of the month
    monthList.forEach(timeOffBreakdownMonthDto -> {
      timeOffBreakdownMonthDto
          .setDate(timeOffBreakdownMonthDto.getDate().withDayOfMonth(1));
    });

    return monthList;
  }

  private List<LocalDateTime> getValidMonthPeriod(Timestamp startDate, Timestamp endDate,
                                                  LocalDateTime selectedDate) {

    LocalDateTime startDateTime = DateUtil.toLocalDateTime(startDate);

    LocalDateTime endDateTime = endDate == null ? selectedDate
        : DateUtil.toLocalDateTime(endDate);

    List<LocalDateTime> validPeriod = new ArrayList<>();

    while (!startDateTime.isAfter(endDateTime)) {
      validPeriod.add(startDateTime);
      startDateTime = startDateTime.plusMonths(1);
    }
    return validPeriod;
  }

  private void addToResultMonthMap(
      TreeMap<Integer, TreeMap<Integer, TimeOffBreakdownMonthDto>> accrualDate,
      List<LocalDateTime> validPeriodList, TimeOffPolicyAccrualSchedule accrualSchedule) {
    for (LocalDateTime currentTime : validPeriodList) {

      TimeOffBreakdownMonthDto timeOffBreakdownMonthDto = transferToMonthDto(accrualSchedule,
          currentTime, accrualSchedule.getAccrualHours() / 12);

      int currentYear = timeOffBreakdownMonthDto.getDate().getYear();
      int currentMonth = timeOffBreakdownMonthDto.getDate().getMonthValue();

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

  private <T> TimeOffBreakdownMonthDto transferToMonthDto(T t, LocalDateTime currentTime,
      Integer monthAccrualHours) {
    TimeOffBreakdownMonthDto timeOffBreakdownMonthDto = new TimeOffBreakdownMonthDto(
        LocalDateTime.of(currentTime.toLocalDate(), LocalTime.MIN),
        monthAccrualHours,
        false
    );

    if (Month.DECEMBER.equals(currentTime.getMonth())) {
      timeOffBreakdownMonthDto.setLastMonthOfTheYear(true);

      TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
      timeOffBreakdownYearDto.setYear(currentTime.getYear());
      BeanUtils.copyProperties(t, timeOffBreakdownYearDto);

      timeOffBreakdownMonthDto.setYearData(timeOffBreakdownYearDto);
    }
    return timeOffBreakdownMonthDto;
  }

  private List<TimeOffBreakdownMonthDto> addMissingMonthDto(
      List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtos) {

    LinkedList<TimeOffBreakdownMonthDto> newTimeOffBreakdownList = new LinkedList<>();

    LocalDateTime previousDate = null;
    for (TimeOffBreakdownMonthDto timeOffBreakdownMonthDto : timeOffBreakdownMonthDtos) {

      TimeOffBreakdownMonthDto previousMonthDto;
      while (previousDate != null && (previousDate = previousDate.plusMonths(1))
          .isBefore(timeOffBreakdownMonthDto.getDate())) {

        TimeOffBreakdownMonthDto newMonthBreakdown = new TimeOffBreakdownMonthDto();
        previousMonthDto = newTimeOffBreakdownList.peekLast();
        BeanUtils.copyProperties(previousMonthDto, newMonthBreakdown);
        newMonthBreakdown.setDate(previousDate);

        // check if the last month of the year and add year data to the last month
        boolean lastMonthOfTheYear = Month.DECEMBER.equals(previousDate.getMonth());
        newMonthBreakdown.setLastMonthOfTheYear(lastMonthOfTheYear);
        newMonthBreakdown.setYearData(lastMonthOfTheYear ? newMonthBreakdown.getYearData() : null);

        newTimeOffBreakdownList.add(newMonthBreakdown);
      }

      newTimeOffBreakdownList.add(timeOffBreakdownMonthDto);
      previousDate = timeOffBreakdownMonthDto.getDate();
    }

    return newTimeOffBreakdownList;
  }

  private TimeOffBreakdownDto getFinalMonthBreakdown(
      List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList,
      TimeOffBreakdownItemDto startingBreakdown, List<TimeOffBreakdownItemDto> balanceAdjustment) {

    TimeOffBalancePojo balancePojo =
        new TimeOffBalancePojo(startingBreakdown.getBalance(), 0);

    TimeOffBreakdownYearDto timeOffBreakdownYearDto = null;
    LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    resultTimeOffBreakdownItemList.add(startingBreakdown);

    for (TimeOffBreakdownMonthDto timeOffBreakdownMonthDto : timeOffBreakdownMonthDtoList) {

      super.populateBreakdownAdjustmentBefore(resultTimeOffBreakdownItemList,
          timeOffBreakdownMonthDto.getDate(), balanceAdjustment, balancePojo);

      // apply carryover limit
      if (timeOffBreakdownYearDto != null
          && balancePojo.getCarryOverLimit() != null
          && balancePojo.getAppliedAccumulation() > balancePojo.getCarryOverLimit()) {

        balancePojo.setAppliedAccumulation(balancePojo.getCarryOverLimit());
      }

      balancePojo.calculateLatestBalance();

      // set new max balance and carryover limit
      if (timeOffBreakdownYearDto != null) {
        balancePojo.setMaxBalance(timeOffBreakdownYearDto.getMaxBalance());
        balancePojo.setCarryOverLimit(timeOffBreakdownYearDto.getCarryoverLimit());
      }

      // stop accrue if reach max balance
      if (balancePojo.reachMaxBalance(false)) {
        balancePojo
            .setBalance(Math.min(balancePojo.getBalance(), balancePojo.getMaxBalance()));
        continue;
      }

      timeOffBreakdownYearDto = timeOffBreakdownMonthDto.getLastMonthOfTheYear()
          ? timeOffBreakdownMonthDto.getYearData()
          : null;

      populateBreakdownListFromMonthDto(resultTimeOffBreakdownItemList, timeOffBreakdownMonthDto,
          balancePojo);
    }
    balancePojo.calculateLatestBalance();
    super.populateRemainingAdjustment(resultTimeOffBreakdownItemList,
        balanceAdjustment, balancePojo);

    TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  private void populateBreakdownListFromMonthDto(
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      TimeOffBreakdownMonthDto timeOffBreakdownMonthDto, TimeOffBalancePojo balancePojo) {

    if (balancePojo.getAppliedAccumulation() == null) {
      balancePojo.setAppliedAccumulation(0);
    }

    Integer newAppliedAccumulation =
        balancePojo.getAppliedAccumulation() + timeOffBreakdownMonthDto.getAccrualHours();
    balancePojo.setAppliedAccumulation(newAppliedAccumulation);

    if (balancePojo.reachMaxBalance(true)) {
      newAppliedAccumulation = balancePojo.getMaxBalance() - balancePojo.getBalance();
      balancePojo.setAppliedAccumulation(newAppliedAccumulation);
    }

    TimeOffBreakdownItemDto timeOffBreakdownItemDto =
        TimeOffBreakdownItemDto.builder()
            .date(timeOffBreakdownMonthDto.getDate())
            .amount(timeOffBreakdownMonthDto.getAccrualHours())
            .balance(balancePojo.getBalance() + balancePojo.getAppliedAccumulation())
            .detail(TIME_OFF_ACCRUED)
            .breakdownType(TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_ACCRUAL)
            .build();
    resultTimeOffBreakdownItemList.add(timeOffBreakdownItemDto);
  }
}
