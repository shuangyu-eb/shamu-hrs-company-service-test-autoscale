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
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
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

  @Override
  TimeOffBreakdownDto getTimeOffBreakdownInternal(final TimeOffBreakdownYearDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    LinkedList<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList = getAccrualDataByYear(
        calculatePojo);
    timeOffBreakdownYearDtoList = addMissingYearDto(timeOffBreakdownYearDtoList);

    timeOffBreakdownYearDtoList.forEach(timeOffBreakdownYearDto -> {
      timeOffBreakdownYearDto.setDate(timeOffBreakdownYearDto.getDate().plusYears(1));
    });

    timeOffBreakdownYearDtoList.add(0, startingBreakdown);

    return getFinalTimeOffBreakdown(timeOffBreakdownYearDtoList,
        calculatePojo.getBalanceAdjustment());
  }

  private LinkedList<TimeOffBreakdownYearDto> getAccrualDataByYear(
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

      final Map breakdownYearMap = getAccrualBreakdownYearMap(validYears, accrualSchedule,
          userJoinDate);
      accrualDate.putAll(breakdownYearMap);

      List<AccrualScheduleMilestone> accrualScheduleMilestoneList =
          accrualScheduleMilestoneRepository
              .findByAccrualScheduleIdWithExpired(accrualSchedule.getId());
      accrualScheduleMilestoneList = trimTimeOffPolicyScheduleMilestones(
          accrualScheduleMilestoneList, calculatePojo.getPolicyUser(),
          accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator
          .comparing(AccrualScheduleMilestone::getAnniversaryYear));

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
            getAccrualBreakdownYearMap(validMilestoneYears, accrualScheduleMilestone,
                milestoneStartTime);
        accrualDate.putAll(milestoneBreakdownYearMap);
      });
    });
    return new LinkedList<>(accrualDate.values());
  }

  private LinkedList<TimeOffBreakdownYearDto> addMissingYearDto(
      final List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtos) {

    final LinkedList<TimeOffBreakdownYearDto> newTimeOffBreakdownList = new LinkedList<>();

    Integer previousYear = null;
    for (final TimeOffBreakdownYearDto timeOffBreakdownYearDto : timeOffBreakdownYearDtos) {
      final Integer currentYear = timeOffBreakdownYearDto.getDate().getYear();

      while (previousYear != null && currentYear > previousYear + 1) {

        final TimeOffBreakdownYearDto previousYearDto = newTimeOffBreakdownList.peekLast();
        final TimeOffBreakdownYearDto newTimeOffYearDto = new TimeOffBreakdownYearDto();
        BeanUtils.copyProperties(previousYearDto, newTimeOffYearDto);
        newTimeOffYearDto.setDate(LocalDate.MIN.withYear(++previousYear));
        newTimeOffYearDto.setHasParent(true);
        newTimeOffBreakdownList.add(newTimeOffYearDto);
      }

      newTimeOffBreakdownList.add(timeOffBreakdownYearDto);
      previousYear = currentYear;
    }
    return newTimeOffBreakdownList;
  }

  private TimeOffBreakdownDto getFinalTimeOffBreakdown(
      final List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList,
      final List<TimeOffBreakdownItemDto> balanceAdjustmentList) {

    timeOffBreakdownYearDtoList.sort(Comparator.comparingInt(y -> y.getDate().getYear()));

    final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    TimeOffBalancePojo balancePojo = new TimeOffBalancePojo(0);

    for (final TimeOffBreakdownYearDto timeOffBreakdownYearDto : timeOffBreakdownYearDtoList) {

      final LocalDate accrualTime = timeOffBreakdownYearDto.getDate();
      // Carryover limit
      super.populateBreakdownListFromCarryoverLimit(resultTimeOffBreakdownItemList,
          accrualTime, balancePojo);

      // base accrual schedule or anniversary accrual milestone
      if (resultTimeOffBreakdownItemList.isEmpty() || !timeOffBreakdownYearDto.isHasParent()) {
        balancePojo.setMaxBalance(timeOffBreakdownYearDto.getMaxBalance());
        balancePojo.setCarryOverLimit(timeOffBreakdownYearDto.getCarryoverLimit());
      }

      // Max balance
      super.populateBreakdownListFromMaxBalance(resultTimeOffBreakdownItemList,
          accrualTime, balancePojo);

      // Accrual
      super.populateBreakdownListFromAccrualSchedule(resultTimeOffBreakdownItemList,
          accrualTime, timeOffBreakdownYearDto.getAccrualHours(), balancePojo);

      // Adjustment
      super.populateBreakdownAdjustmentBefore(resultTimeOffBreakdownItemList,
          accrualTime.plusYears(1),
          balanceAdjustmentList, balancePojo);
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
      final List<Integer> validYears, final T t, final LocalDate parentBaseDate) {
    final HashMap breakdownYear = new HashMap();

    for (int index = 0, len = validYears.size(); index < len; index++) {
      Integer currentYear = validYears.get(index);
      final TimeOffBreakdownYearDto timeOffBreakdownYearDto = transferToTimeOffYearDto(t);
      timeOffBreakdownYearDto.setDate(LocalDate.MIN.withYear(currentYear));
      timeOffBreakdownYearDto.setHasParent(parentBaseDate.getYear() != currentYear);
      breakdownYear.put(currentYear, timeOffBreakdownYearDto);
    }
    return breakdownYear;
  }

  private <T> TimeOffBreakdownYearDto transferToTimeOffYearDto(final T t) {
    final TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    BeanUtils.copyProperties(t, timeOffBreakdownYearDto);
    return timeOffBreakdownYearDto;
  }

  @Override
  boolean support(String frequencyType) {
    return AccrualFrequencyType.FREQUENCY_TYPE_ONE.equalsTo(frequencyType);
  }
}
