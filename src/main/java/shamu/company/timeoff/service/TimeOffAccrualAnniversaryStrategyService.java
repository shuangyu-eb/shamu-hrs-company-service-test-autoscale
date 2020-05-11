package shamu.company.timeoff.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.dto.TimeOffBreakdownAnniversaryDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
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

  @Override
  TimeOffBreakdownDto getTimeOffBreakdownInternal(
      final TimeOffBreakdownYearDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {
    final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto =
        new TimeOffBreakdownAnniversaryDto();
    BeanUtils.copyProperties(startingBreakdown, timeOffBreakdownAnniversaryDto);

    return getTimeOffBreakdown(timeOffBreakdownAnniversaryDto, calculatePojo);
  }

  TimeOffBreakdownDto getTimeOffBreakdown(
      final TimeOffBreakdownAnniversaryDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList =
        getAccrualDataByAnniversaryYear(calculatePojo);

    timeOffBreakdownAnniversaryDtoList =
        addMissingAnniversaryYearDto(timeOffBreakdownAnniversaryDtoList);

    timeOffBreakdownAnniversaryDtoList.forEach(
        timeOffBreakdownAnniversaryDto ->
            timeOffBreakdownAnniversaryDto.setDate(
                timeOffBreakdownAnniversaryDto
                    .getDate()
                    .plusYears(1)
                    .withDayOfMonth(startingBreakdown.getDate().getDayOfMonth())));

    timeOffBreakdownAnniversaryDtoList.add(0, startingBreakdown);

    return getFinalAnniversaryBreakdown(
        timeOffBreakdownAnniversaryDtoList, calculatePojo.getBalanceAdjustment());
  }

  private List<TimeOffBreakdownAnniversaryDto> getAccrualDataByAnniversaryYear(
      final TimeOffBreakdownCalculatePojo calculatePojo) {
    final LocalDate userJoinDate =
        DateUtil.fromTimestamp(calculatePojo.getPolicyUser().getCreatedAt());

    final HashMap<Integer, HashMap<Integer, TimeOffBreakdownAnniversaryDto>> accrualData =
        new HashMap<>();

    calculatePojo
        .getTrimmedScheduleList()
        .forEach(
            accrualSchedule -> {
              final List<LocalDate> validStartAndEndDate =
                  getValidScheduleOrMilestonePeriod(
                      userJoinDate, accrualSchedule.getCreatedAt(), accrualSchedule.getExpiredAt());

              final List<LocalDate> dateList =
                  getValidAnniversaryPeriod(
                      userJoinDate,
                      validStartAndEndDate.get(0),
                      validStartAndEndDate.get(1),
                      calculatePojo.getUntilDate());

              addToResultAnniversaryMap(accrualData, dateList, accrualSchedule, userJoinDate);

              List<AccrualScheduleMilestone> accrualScheduleMilestoneList =
                  accrualScheduleMilestoneRepository.findByAccrualScheduleIdWithExpired(
                      accrualSchedule.getId());
              accrualScheduleMilestoneList =
                  trimTimeOffPolicyScheduleMilestones(
                      accrualScheduleMilestoneList, calculatePojo.getPolicyUser(), accrualSchedule);

              // sort milestones
              accrualScheduleMilestoneList.sort(
                  Comparator.comparing(AccrualScheduleMilestone::getAnniversaryYear));

              accrualScheduleMilestoneList.forEach(
                  accrualScheduleMilestone -> {
                    final LocalDate milestoneStartTime =
                        userJoinDate.plusYears(accrualScheduleMilestone.getAnniversaryYear());
                    final List<LocalDate> validMilestoneStartAndEndDate =
                        getValidScheduleOrMilestonePeriod(
                            milestoneStartTime,
                            accrualScheduleMilestone.getCreatedAt(),
                            accrualScheduleMilestone.getExpiredAt());

                    final List<LocalDate> validMilestoneYears =
                        getValidAnniversaryPeriod(
                            userJoinDate,
                            validMilestoneStartAndEndDate.get(0),
                            validMilestoneStartAndEndDate.get(1),
                            calculatePojo.getUntilDate());

                    addToResultAnniversaryMap(
                        accrualData,
                        validMilestoneYears,
                        accrualScheduleMilestone,
                        milestoneStartTime);
                  });
            });

    final List<TimeOffBreakdownAnniversaryDto> resultAnniversaryList = new ArrayList<>();
    accrualData
        .values()
        .forEach(
            timeOffAnniversaryMonthMap ->
                resultAnniversaryList.addAll(timeOffAnniversaryMonthMap.values()));

    return resultAnniversaryList;
  }

  private List<LocalDate> getValidAnniversaryPeriod(
      final LocalDate userJoinPolicyDate,
      final LocalDate startDate,
      final LocalDate endDate,
      final LocalDate selectedDate) {

    LocalDate currentDate = userJoinPolicyDate;
    final LocalDate expireDate = endDate == null ? selectedDate : endDate;

    final List<LocalDate> startDateList = new ArrayList<>();

    while (!(currentDate = currentDate.plusYears(1)).isAfter(expireDate)) {
      if (!currentDate.isBefore(startDate)) {
        startDateList.add(currentDate.minusYears(1));
      }
    }

    return startDateList;
  }

  private <T> void addToResultAnniversaryMap(
      final HashMap<Integer, HashMap<Integer, TimeOffBreakdownAnniversaryDto>> accrualData,
      final List<LocalDate> validPeriods,
      final T t,
      final LocalDate anniversaryStartDate) {

    for (final LocalDate date : validPeriods) {
      final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto =
          transferToTimeOffAnniversaryYearDto(t);
      timeOffBreakdownAnniversaryDto.setDate(date);

      final boolean isParent =
          !anniversaryStartDate.isAfter(date) && anniversaryStartDate.plusYears(1).isAfter(date);
      timeOffBreakdownAnniversaryDto.setHasParent(!isParent);

      final Integer currentYear = date.getYear();
      final Integer currentMonth = date.getMonthValue();

      final Map<Integer, TimeOffBreakdownAnniversaryDto> currentYearMap =
          accrualData.get(currentYear);
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
    final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto =
        new TimeOffBreakdownAnniversaryDto();
    BeanUtils.copyProperties(t, timeOffBreakdownAnniversaryDto);
    return timeOffBreakdownAnniversaryDto;
  }

  private List<TimeOffBreakdownAnniversaryDto> addMissingAnniversaryYearDto(
      final List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList) {
    final LinkedList<TimeOffBreakdownAnniversaryDto> newBreakdownList = new LinkedList<>();

    LocalDate previousDate = null;

    for (final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto :
        timeOffBreakdownAnniversaryDtoList) {

      final LocalDate currentDate = timeOffBreakdownAnniversaryDto.getDate();

      while (previousDate != null
          && (previousDate = previousDate.plusYears(1)).isBefore(currentDate)) {

        final TimeOffBreakdownAnniversaryDto previousBreakdown = newBreakdownList.peekLast();
        final TimeOffBreakdownAnniversaryDto newTimeOffBreakdown =
            new TimeOffBreakdownAnniversaryDto();
        BeanUtils.copyProperties(previousBreakdown, newTimeOffBreakdown);
        newTimeOffBreakdown.setDate(previousDate);
        newTimeOffBreakdown.setHasParent(true);
        newBreakdownList.add(newTimeOffBreakdown);
      }

      newBreakdownList.add(timeOffBreakdownAnniversaryDto);
      previousDate = currentDate;
    }

    return newBreakdownList;
  }

  private TimeOffBreakdownDto getFinalAnniversaryBreakdown(
      final List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList,
      final List<TimeOffBreakdownItemDto> balanceAdjustmentList) {

    final TimeOffBalancePojo balancePojo = new TimeOffBalancePojo(0);

    final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();

    for (final TimeOffBreakdownAnniversaryDto timeOffBreakdownAnniversaryDto :
        timeOffBreakdownAnniversaryDtoList) {

      final LocalDate accrualTime = timeOffBreakdownAnniversaryDto.getDate();

      // carryover limit
      populateBreakdownListFromCarryoverLimit(
          resultTimeOffBreakdownItemList, accrualTime, balancePojo);

      if (resultTimeOffBreakdownItemList.isEmpty()
          || !timeOffBreakdownAnniversaryDto.isHasParent()) {
        balancePojo.setMaxBalance(timeOffBreakdownAnniversaryDto.getMaxBalance());
        balancePojo.setCarryOverLimit(timeOffBreakdownAnniversaryDto.getCarryoverLimit());
      }

      // max balance
      populateBreakdownListFromMaxBalance(resultTimeOffBreakdownItemList, accrualTime, balancePojo);

      populateBreakdownListFromAccrualSchedule(
          resultTimeOffBreakdownItemList,
          accrualTime,
          timeOffBreakdownAnniversaryDto.getAccrualHours(),
          balancePojo);

      // Adjustment
      populateBreakdownAdjustmentBefore(
          resultTimeOffBreakdownItemList,
          accrualTime.plusYears(1),
          balanceAdjustmentList,
          balancePojo);
    }

    populateRemainingAdjustment(resultTimeOffBreakdownItemList, balanceAdjustmentList, balancePojo);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  @Override
  boolean support(final String frequencyType) {
    return AccrualFrequencyType.FREQUENCY_TYPE_TWO.equalsTo(frequencyType);
  }
}
