package shamu.company.timeoff.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.BaseEntity;
import shamu.company.timeoff.dto.TimeOffBreakdownAnniversaryDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.user.entity.User;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffAccrualAnniversaryStrategyServiceImpl extends TimeOffAccrualServiceImpl {

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Autowired
  public TimeOffAccrualAnniversaryStrategyServiceImpl(
      AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository) {
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(TimeOffBreakdownItemDto startingBreakdown,
      TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList =
        getAccrualDataByAnniversaryYear(calculatePojo.getTrimmedScheduleList(),
            calculatePojo.getPolicyUser().getUser());

    timeOffBreakdownAnniversaryDtoList = addMissingAnniversaryYearDto(
        timeOffBreakdownAnniversaryDtoList);

    return getFinalAnniversaryBreakdown(timeOffBreakdownAnniversaryDtoList,
        startingBreakdown, calculatePojo.getBalanceAdjustment());
  }


  private List<TimeOffBreakdownAnniversaryDto> getAccrualDataByAnniversaryYear(
      List<TimeOffPolicyAccrualSchedule> trimmedScheduleList, User user) {
    LocalDateTime userJoinDateTime = DateUtil.toLocalDateTime(user.getCreatedAt());

    HashMap<Integer, HashMap<Integer, TimeOffBreakdownAnniversaryDto>> accrualData
        = new HashMap<>();

    trimmedScheduleList.forEach(accrualSchedule -> {

      List<Timestamp> validStartAndEndDate =
          getValidAccrualScheduleStartAndEndDate(userJoinDateTime, accrualSchedule);

      List<LocalDateTime> dateList =
          getValidAnniversaryPeriod(Timestamp.valueOf(userJoinDateTime),
              validStartAndEndDate.get(0), validStartAndEndDate.get(1));

      addToResultAnniversaryMap(accrualData, dateList, accrualSchedule);

      List<AccrualScheduleMilestone> accrualScheduleMilestoneList =
          accrualScheduleMilestoneRepository
              .findByTimeOffPolicyAccrualScheduleId(accrualSchedule.getId());
      accrualScheduleMilestoneList =
          trimTimeOffPolicyScheduleMilestones(accrualScheduleMilestoneList, user, accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator.comparing(BaseEntity::getCreatedAt));

      accrualScheduleMilestoneList.forEach(accrualScheduleMilestone -> {
        List<Timestamp> validMilestoneStartAndEndDate =
            getValidMilestoneStartAndEndDate(userJoinDateTime, accrualScheduleMilestone);

        List<LocalDateTime> validMilestoneYears = getValidAnniversaryPeriod(
            Timestamp.valueOf(userJoinDateTime),
            validMilestoneStartAndEndDate.get(0),
            validMilestoneStartAndEndDate.get(1));

        addToResultAnniversaryMap(accrualData, validMilestoneYears, accrualScheduleMilestone);
      });
    });

    List<TimeOffBreakdownAnniversaryDto> resultAnniversaryList = new ArrayList<>();
    accrualData.values().forEach(timeOffAnniversaryMonthMap ->
        resultAnniversaryList.addAll(timeOffAnniversaryMonthMap.values()));

    return resultAnniversaryList;
  }

  private List<LocalDateTime> getValidAnniversaryPeriod(Timestamp userEnrollTime,
      Timestamp startDate, Timestamp endDate) {

    LocalDateTime userEnrollDate = DateUtil.toLocalDateTime(userEnrollTime);
    LocalDateTime createDate = DateUtil.toLocalDateTime(startDate);
    LocalDateTime expireDate = endDate == null ? LocalDateTime.now()
        : DateUtil.toLocalDateTime(endDate);

    List<LocalDateTime> startDateList = new ArrayList<>();

    while (!(userEnrollDate = userEnrollDate.plusDays(365)).isAfter(expireDate)) {

      if (userEnrollDate.minusDays(365).isAfter(createDate)) {
        startDateList.add(userEnrollDate.minusDays(365));
      }
    }
    return startDateList;
  }

  private <T> void addToResultAnniversaryMap(
      HashMap<Integer, HashMap<Integer, TimeOffBreakdownAnniversaryDto>> accrualData,
      List<LocalDateTime> validPeriods, T t) {

    for (LocalDateTime date : validPeriods) {
      TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto =
          transferToTimeOffAnniversaryYearDto(t);
      timeOffBreakdownAnniversaryDto.setDate(date);

      Integer currentYear = date.getYear();
      Integer currentMonth = date.getMonthValue();

      Map<Integer, TimeOffBreakdownAnniversaryDto> currentYearMap = accrualData.get(currentYear);
      if (currentYearMap != null) {
        currentYearMap.put(currentMonth, timeOffBreakdownAnniversaryDto);
      } else {
        HashMap<Integer, TimeOffBreakdownAnniversaryDto> newMonthMap = new HashMap<>();
        newMonthMap.put(currentMonth, timeOffBreakdownAnniversaryDto);
        accrualData.put(currentYear, newMonthMap);
      }
    }
  }

  private <T> TimeOffBreakdownAnniversaryDto transferToTimeOffAnniversaryYearDto(T t) {
    TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto
        = new TimeOffBreakdownAnniversaryDto();
    BeanUtils.copyProperties(t, timeOffBreakdownAnniversaryDto);
    return timeOffBreakdownAnniversaryDto;
  }

  private List<TimeOffBreakdownAnniversaryDto> addMissingAnniversaryYearDto(
      List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList) {
    LinkedList<TimeOffBreakdownAnniversaryDto> newBreakdownList = new LinkedList<>();

    LocalDateTime previousDate = null;

    for (TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto
        : timeOffBreakdownAnniversaryDtoList) {

      LocalDateTime currentDate = timeOffBreakdownAnniversaryDto.getDate();

      while (previousDate != null && (previousDate = previousDate.plusYears(1))
          .isBefore(currentDate)) {

        TimeOffBreakdownAnniversaryDto previousBreakdown = newBreakdownList.peekLast();
        TimeOffBreakdownAnniversaryDto newTimeOffBreakdown = new TimeOffBreakdownAnniversaryDto();
        BeanUtils.copyProperties(previousBreakdown, newTimeOffBreakdown);
        newTimeOffBreakdown.setDate(previousDate);
        newBreakdownList.add(newTimeOffBreakdown);
      }

      newBreakdownList.add(timeOffBreakdownAnniversaryDto);
      previousDate = currentDate;
    }

    return newBreakdownList;
  }

  private TimeOffBreakdownDto getFinalAnniversaryBreakdown(
      List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList,
      TimeOffBreakdownItemDto startingBreakdown,
      List<TimeOffBreakdownItemDto> balanceAdjustmentList) {

    TimeOffBalancePojo balancePojo =
        new TimeOffBalancePojo(startingBreakdown.getBalance(), 0);

    LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    resultTimeOffBreakdownItemList.add(startingBreakdown);

    for (TimeOffBreakdownAnniversaryDto timeOffBreakdownYearDto :
        timeOffBreakdownAnniversaryDtoList) {
      super.populateBreakdownAdjustmentBefore(resultTimeOffBreakdownItemList,
          timeOffBreakdownYearDto.getDate(), balanceAdjustmentList, balancePojo);

      // apply carryoverLimit
      TimeOffBreakdownItemDto lastTimeOffBreakdown = resultTimeOffBreakdownItemList.peekLast();
      super.tuneBalanceWithCarryOverLimit(lastTimeOffBreakdown, balancePojo);

      balancePojo.calculateLatestBalance();
      balancePojo.setMaxBalance(timeOffBreakdownYearDto.getMaxBalance());
      balancePojo.setCarryOverLimit(timeOffBreakdownYearDto.getCarryoverLimit());

      // stop accrue if reach max balance
      if (balancePojo.reachMaxBalance(false)) {
        balancePojo.setBalance(
            Math.min(balancePojo.getBalance(), balancePojo.getMaxBalance()));
        continue;
      }

      populateBreakdownListFromAnniversaryYearDto(resultTimeOffBreakdownItemList,
          timeOffBreakdownYearDto, balancePojo);
    }

    balancePojo.calculateLatestBalance();
    super.populateRemainingAdjustment(resultTimeOffBreakdownItemList, balanceAdjustmentList,
        balancePojo);

    TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  private void populateBreakdownListFromAnniversaryYearDto(
      LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      TimeOffBreakdownAnniversaryDto timeOffBreakdownYearDto, TimeOffBalancePojo balancePojo) {

    if (balancePojo.getAppliedAccumulation() == null) {
      balancePojo.setAppliedAccumulation(0);
    }

    balancePojo.setAppliedAccumulation(
        balancePojo.getAppliedAccumulation() + timeOffBreakdownYearDto.getAccrualHours());

    if (balancePojo.reachMaxBalance(true)) {
      int newAppliedAccumulation = balancePojo.getMaxBalance() - balancePojo.getBalance();
      balancePojo.setAppliedAccumulation(newAppliedAccumulation);
    }


    TimeOffBreakdownItemDto timeOffBreakdownItemDto =
        TimeOffBreakdownItemDto.builder()
            .date(timeOffBreakdownYearDto.getDate())
            .detail(TIME_OFF_ACCRUED)
            .amount(timeOffBreakdownYearDto.getAccrualHours())
            .balance(balancePojo.getBalance() + balancePojo.getAppliedAccumulation())
            .breakdownType(TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_ACCRUAL)
            .build();
    resultTimeOffBreakdownItemList.add(timeOffBreakdownItemDto);
  }
}
