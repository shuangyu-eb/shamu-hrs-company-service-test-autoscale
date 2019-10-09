package shamu.company.timeoff.service;

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
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffAccrualAnniversaryStrategyService extends TimeOffAccrualService {

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Autowired
  public TimeOffAccrualAnniversaryStrategyService(
      final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository) {
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(final TimeOffBreakdownItemDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList =
        getAccrualDataByAnniversaryYear(calculatePojo);

    timeOffBreakdownAnniversaryDtoList = addMissingAnniversaryYearDto(
        timeOffBreakdownAnniversaryDtoList);

    timeOffBreakdownAnniversaryDtoList.forEach(timeOffBreakdownAnniversaryDto -> {
      timeOffBreakdownAnniversaryDto.setDate(timeOffBreakdownAnniversaryDto.getDate().plusYears(1));
    });

    return getFinalAnniversaryBreakdown(timeOffBreakdownAnniversaryDtoList,
        startingBreakdown, calculatePojo.getBalanceAdjustment());
  }


  private List<TimeOffBreakdownAnniversaryDto> getAccrualDataByAnniversaryYear(
      final TimeOffBreakdownCalculatePojo calculatePojo) {
    final LocalDateTime userJoinDateTime = DateUtil.toLocalDateTime(
        calculatePojo.getPolicyUser().getUser().getCreatedAt());

    final HashMap<Integer, HashMap<Integer, TimeOffBreakdownAnniversaryDto>> accrualData
        = new HashMap<>();

    calculatePojo.getTrimmedScheduleList().forEach(accrualSchedule -> {

      final List<Timestamp> validStartAndEndDate =
          getValidAccrualScheduleStartAndEndDate(userJoinDateTime, accrualSchedule);

      final List<LocalDateTime> dateList =
          getValidAnniversaryPeriod(Timestamp.valueOf(userJoinDateTime),
              validStartAndEndDate.get(0),
              validStartAndEndDate.get(1),
              calculatePojo.getUntilDate()
          );

      addToResultAnniversaryMap(accrualData, dateList, accrualSchedule);

      List<AccrualScheduleMilestone> accrualScheduleMilestoneList =
          accrualScheduleMilestoneRepository
              .findByTimeOffPolicyAccrualScheduleId(accrualSchedule.getId());
      accrualScheduleMilestoneList =
          trimTimeOffPolicyScheduleMilestones(accrualScheduleMilestoneList,
              calculatePojo.getPolicyUser().getUser(), accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator.comparing(BaseEntity::getCreatedAt));

      accrualScheduleMilestoneList.forEach(accrualScheduleMilestone -> {
        final List<Timestamp> validMilestoneStartAndEndDate =
            getValidMilestoneStartAndEndDate(userJoinDateTime, accrualScheduleMilestone);

        final List<LocalDateTime> validMilestoneYears = getValidAnniversaryPeriod(
            Timestamp.valueOf(userJoinDateTime),
            validMilestoneStartAndEndDate.get(0),
            validMilestoneStartAndEndDate.get(1),
            calculatePojo.getUntilDate()
        );

        addToResultAnniversaryMap(accrualData, validMilestoneYears, accrualScheduleMilestone);
      });
    });

    final List<TimeOffBreakdownAnniversaryDto> resultAnniversaryList = new ArrayList<>();
    accrualData.values().forEach(timeOffAnniversaryMonthMap ->
        resultAnniversaryList.addAll(timeOffAnniversaryMonthMap.values()));

    return resultAnniversaryList;
  }

  private List<LocalDateTime> getValidAnniversaryPeriod(final Timestamp userEnrollTime,
      final Timestamp startDate, final Timestamp endDate, final LocalDateTime selectedDate) {

    LocalDateTime userEnrollDate = DateUtil.toLocalDateTime(userEnrollTime);
    final LocalDateTime createDate = DateUtil.toLocalDateTime(startDate);
    final LocalDateTime expireDate = endDate == null ? selectedDate
        : DateUtil.toLocalDateTime(endDate);

    final List<LocalDateTime> startDateList = new ArrayList<>();

    while (!(userEnrollDate = userEnrollDate.plusDays(365)).isAfter(expireDate)) {

      if (userEnrollDate.minusDays(365).isAfter(createDate)) {
        startDateList.add(userEnrollDate.minusDays(365));
      }
    }
    return startDateList;
  }

  private <T> void addToResultAnniversaryMap(
      final HashMap<Integer, HashMap<Integer, TimeOffBreakdownAnniversaryDto>> accrualData,
      final List<LocalDateTime> validPeriods, final T t) {

    for (final LocalDateTime date : validPeriods) {
      final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto =
          transferToTimeOffAnniversaryYearDto(t);
      timeOffBreakdownAnniversaryDto.setDate(date);

      final Integer currentYear = date.getYear();
      final Integer currentMonth = date.getMonthValue();

      final Map<Integer, TimeOffBreakdownAnniversaryDto> currentYearMap = accrualData
          .get(currentYear);
      if (currentYearMap != null) {
        currentYearMap.put(currentMonth, timeOffBreakdownAnniversaryDto);
      } else {
        final HashMap<Integer, TimeOffBreakdownAnniversaryDto> newMonthMap = new HashMap<>();
        newMonthMap.put(currentMonth, timeOffBreakdownAnniversaryDto);
        accrualData.put(currentYear, newMonthMap);
      }
    }
  }

  private <T> TimeOffBreakdownAnniversaryDto transferToTimeOffAnniversaryYearDto(final T t) {
    final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto
        = new TimeOffBreakdownAnniversaryDto();
    BeanUtils.copyProperties(t, timeOffBreakdownAnniversaryDto);
    return timeOffBreakdownAnniversaryDto;
  }

  private List<TimeOffBreakdownAnniversaryDto> addMissingAnniversaryYearDto(
      final List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList) {
    final LinkedList<TimeOffBreakdownAnniversaryDto> newBreakdownList = new LinkedList<>();

    LocalDateTime previousDate = null;

    for (final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto
        : timeOffBreakdownAnniversaryDtoList) {

      final LocalDateTime currentDate = timeOffBreakdownAnniversaryDto.getDate();

      while (previousDate != null && (previousDate = previousDate.plusYears(1))
          .isBefore(currentDate)) {

        final TimeOffBreakdownAnniversaryDto previousBreakdown = newBreakdownList.peekLast();
        final TimeOffBreakdownAnniversaryDto newTimeOffBreakdown
            = new TimeOffBreakdownAnniversaryDto();
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
      final List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList,
      final TimeOffBreakdownItemDto startingBreakdown,
      final List<TimeOffBreakdownItemDto> balanceAdjustmentList) {

    final TimeOffBalancePojo balancePojo =
        new TimeOffBalancePojo(startingBreakdown.getBalance(), 0);

    final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    resultTimeOffBreakdownItemList.add(startingBreakdown);

    for (final TimeOffBreakdownAnniversaryDto timeOffBreakdownYearDto :
        timeOffBreakdownAnniversaryDtoList) {
      super.populateBreakdownAdjustmentBefore(resultTimeOffBreakdownItemList,
          timeOffBreakdownYearDto.getDate(), balanceAdjustmentList, balancePojo);

      // apply carryoverLimit
      final TimeOffBreakdownItemDto lastTimeOffBreakdown = resultTimeOffBreakdownItemList
          .peekLast();
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

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  private void populateBreakdownListFromAnniversaryYearDto(
      final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final TimeOffBreakdownAnniversaryDto timeOffBreakdownYearDto,
      final TimeOffBalancePojo balancePojo) {

    if (balancePojo.getAppliedAccumulation() == null) {
      balancePojo.setAppliedAccumulation(0);
    }

    balancePojo.setAppliedAccumulation(
        balancePojo.getAppliedAccumulation() + timeOffBreakdownYearDto.getAccrualHours());

    if (balancePojo.reachMaxBalance(true)) {
      final int newAppliedAccumulation = balancePojo.getMaxBalance() - balancePojo.getBalance();
      balancePojo.setAppliedAccumulation(newAppliedAccumulation);
    }

    final TimeOffBreakdownItemDto timeOffBreakdownItemDto =
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
