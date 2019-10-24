package shamu.company.timeoff.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffAccrualNatureStrategyService extends TimeOffAccrualService {

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Autowired
  public TimeOffAccrualNatureStrategyService(
      final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository) {
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
  }

  TimeOffBreakdownDto getTimeOffBreakdown(final TimeOffBreakdownItemDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList = getAccrualDataByYear(
        calculatePojo);
    timeOffBreakdownYearDtoList = addMissingYearDto(timeOffBreakdownYearDtoList);

    timeOffBreakdownYearDtoList.forEach(timeOffBreakdownYearDto -> {
      timeOffBreakdownYearDto.setYear(timeOffBreakdownYearDto.getYear() + 1);
    });

    return getFinalTimeOffBreakdown(timeOffBreakdownYearDtoList,
        startingBreakdown, calculatePojo.getBalanceAdjustment());
  }

  private List<TimeOffBreakdownYearDto> getAccrualDataByYear(
      final TimeOffBreakdownCalculatePojo calculatePojo) {
    final LocalDate userJoinDate =
        DateUtil.fromTimestamp(calculatePojo.getPolicyUser().getCreatedAt());

    final HashMap<Integer, TimeOffBreakdownYearDto> accrualDate = new HashMap<>();
    calculatePojo.getTrimmedScheduleList().forEach(accrualSchedule -> {

      final List<LocalDate> validStartAndEndDate =
          super.getValidScheduleOrMilestonePeriod(userJoinDate,
              accrualSchedule.getCreatedAt(), accrualSchedule.getExpiredAt());

      final List<Integer> validYears =
          getValidYearPeriod(
              validStartAndEndDate.get(0),
              validStartAndEndDate.get(1),
              calculatePojo.getUntilDate()
          );

      final Map breakdownYearMap = getAccrualBreakdownYearMap(validYears, accrualSchedule);
      accrualDate.putAll(breakdownYearMap);

      List<AccrualScheduleMilestone> accrualScheduleMilestoneList =
          accrualScheduleMilestoneRepository
              .findByTimeOffPolicyAccrualScheduleId(accrualSchedule.getId());
      accrualScheduleMilestoneList = trimTimeOffPolicyScheduleMilestones(
          accrualScheduleMilestoneList, calculatePojo.getPolicyUser(),
          accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator
          .comparing(AccrualScheduleMilestone::getCreatedAt, Comparator.reverseOrder()));

      accrualScheduleMilestoneList.forEach(accrualScheduleMilestone -> {

        LocalDate milestoneStartTime = userJoinDate
            .plusYears(accrualScheduleMilestone.getAnniversaryYear());
        final List<LocalDate> validMilestoneStartAndEndDate =
            super.getValidScheduleOrMilestonePeriod(milestoneStartTime,
                accrualScheduleMilestone.getCreatedAt(), accrualScheduleMilestone.getExpiredAt());

        final List<Integer> validMilestoneYears = getValidYearPeriod(
            validMilestoneStartAndEndDate.get(0),
            validMilestoneStartAndEndDate.get(1),
            calculatePojo.getUntilDate()
        );
        final Map milestoneBreakdownYearMap =
            getAccrualBreakdownYearMap(validMilestoneYears, accrualScheduleMilestone);
        accrualDate.putAll(milestoneBreakdownYearMap);
      });
    });
    return new ArrayList<>(accrualDate.values());
  }

  private LinkedList<TimeOffBreakdownYearDto> addMissingYearDto(
      final List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtos) {

    final LinkedList<TimeOffBreakdownYearDto> newTimeOffBreakdownList = new LinkedList<>();

    Integer previousYear = null;
    for (final TimeOffBreakdownYearDto timeOffBreakdownYearDto : timeOffBreakdownYearDtos) {
      final Integer currentYear = timeOffBreakdownYearDto.getYear();

      while (previousYear != null && currentYear > previousYear + 1) {

        final TimeOffBreakdownYearDto previousYearDto = newTimeOffBreakdownList.peekLast();
        final TimeOffBreakdownYearDto newTimeOffYearDto = new TimeOffBreakdownYearDto();
        BeanUtils.copyProperties(previousYearDto, newTimeOffYearDto);
        newTimeOffYearDto.setYear(++previousYear);
        newTimeOffBreakdownList.add(newTimeOffYearDto);
      }

      newTimeOffBreakdownList.add(timeOffBreakdownYearDto);
      previousYear = currentYear;
    }
    return newTimeOffBreakdownList;
  }

  private TimeOffBreakdownDto getFinalTimeOffBreakdown(
      final List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList,
      final TimeOffBreakdownItemDto startingBreakdown,
      final List<TimeOffBreakdownItemDto> balanceAdjustmentList) {

    timeOffBreakdownYearDtoList.sort(Comparator.comparingInt(TimeOffBreakdownYearDto::getYear));

    final TimeOffBalancePojo balancePojo =
        new TimeOffBalancePojo(startingBreakdown.getBalance());

    final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    resultTimeOffBreakdownItemList.add(startingBreakdown);

    for (final TimeOffBreakdownYearDto timeOffBreakdownYearDto : timeOffBreakdownYearDtoList) {

      final LocalDate accrualTime = LocalDate.MIN.withYear(timeOffBreakdownYearDto.getYear());
      // Adjustment
      super.populateBreakdownAdjustmentBefore(resultTimeOffBreakdownItemList, accrualTime,
          balanceAdjustmentList, balancePojo);

      // Carryover limit
      super.populateBreakdownListFromCarryoverLimit(resultTimeOffBreakdownItemList,
          accrualTime, balancePojo);

      // Accrual
      super.populateBreakdownListFromAccrualSchedule(resultTimeOffBreakdownItemList,
          accrualTime, timeOffBreakdownYearDto.getAccrualHours(), balancePojo);

      balancePojo.setMaxBalance(timeOffBreakdownYearDto.getMaxBalance());
      balancePojo.setCarryOverLimit(timeOffBreakdownYearDto.getCarryoverLimit());

      // Max balance
      super.populateBreakdownListFromMaxBalance(resultTimeOffBreakdownItemList,
          accrualTime, balancePojo);
    }
    super.populateRemainingAdjustment(resultTimeOffBreakdownItemList, balanceAdjustmentList,
        balancePojo);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  private List<Integer> getValidYearPeriod(final LocalDate createdAt, final LocalDate expiredAt,
      final LocalDate selectedDate) {
    final LocalDate expireDate = expiredAt == null ? selectedDate : expiredAt;

    Integer startYear = createdAt.getYear();
    final Integer endYear = expireDate.getYear();

    if (startYear > endYear) {
      return Collections.emptyList();
    }

    final List<Integer> validYear = new ArrayList<>();
    while (startYear <= endYear) {
      validYear.add(startYear++);
    }
    return validYear;
  }

  private <T> Map<Integer, TimeOffBreakdownItemDto> getAccrualBreakdownYearMap(
      final List<Integer> validYears, final T t) {
    final HashMap breakdownYear = new HashMap();

    for (final Integer currentYear : validYears) {
      final TimeOffBreakdownYearDto timeOffBreakdownYearDto = transferToTimeOffYearDto(t);
      timeOffBreakdownYearDto.setYear(currentYear);
      breakdownYear.put(currentYear, timeOffBreakdownYearDto);
    }
    return breakdownYear;
  }

  private <T> TimeOffBreakdownYearDto transferToTimeOffYearDto(final T t) {
    final TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    BeanUtils.copyProperties(t, timeOffBreakdownYearDto);
    return timeOffBreakdownYearDto;
  }

}
