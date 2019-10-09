package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import shamu.company.common.entity.BaseEntity;
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

  public TimeOffBreakdownDto getTimeOffBreakdown(final TimeOffBreakdownItemDto startingBreakdown,
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
    final LocalDateTime userJoinDateTime = DateUtil.toLocalDateTime(
        calculatePojo.getPolicyUser().getUser().getCreatedAt());

    final HashMap<Integer, TimeOffBreakdownYearDto> accrualDate = new HashMap<>();
    calculatePojo.getTrimmedScheduleList().forEach(accrualSchedule -> {

      final List<Timestamp> validStartAndEndDate =
          getValidAccrualScheduleStartAndEndDate(userJoinDateTime, accrualSchedule);
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
          accrualScheduleMilestoneList, calculatePojo.getPolicyUser().getUser(),
          accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator.comparing(BaseEntity::getCreatedAt));

      accrualScheduleMilestoneList.forEach(accrualScheduleMilestone -> {
        final List<Timestamp> validMilestoneStartAndEndDate =
            getValidMilestoneStartAndEndDate(userJoinDateTime, accrualScheduleMilestone);
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
        new TimeOffBalancePojo(startingBreakdown.getBalance(), 0);

    final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    resultTimeOffBreakdownItemList.add(startingBreakdown);

    for (final TimeOffBreakdownYearDto timeOffBreakdownYearDto : timeOffBreakdownYearDtoList) {
      // apply carryoverLimit
      final TimeOffBreakdownItemDto lastTimeOffBreakdown = resultTimeOffBreakdownItemList
          .peekLast();

      final LocalDate untilDate = LocalDate.MIN.withYear(timeOffBreakdownYearDto.getYear());
      final LocalDateTime untilDateTime = LocalDateTime.of(untilDate, LocalTime.MIN);
      super.populateBreakdownAdjustmentBefore(resultTimeOffBreakdownItemList, untilDateTime,
          balanceAdjustmentList, balancePojo);

      super.tuneBalanceWithCarryOverLimit(lastTimeOffBreakdown, balancePojo);
      balancePojo.setMaxBalance(timeOffBreakdownYearDto.getMaxBalance());
      balancePojo.setCarryOverLimit(timeOffBreakdownYearDto.getCarryoverLimit());
      balancePojo.calculateLatestBalance();

      // stop accrue if reach max balance
      if (balancePojo.reachMaxBalance(false)) {
        balancePojo.setBalance(
            Math.min(balancePojo.getBalance(), balancePojo.getMaxBalance()));
        continue;
      }

      populateBreakdownListFromYearDto(resultTimeOffBreakdownItemList, timeOffBreakdownYearDto,
          balancePojo);
    }
    balancePojo.calculateLatestBalance();
    super.populateRemainingAdjustment(resultTimeOffBreakdownItemList, balanceAdjustmentList,
        balancePojo);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  private void populateBreakdownListFromYearDto(
      final List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      final TimeOffBreakdownYearDto timeOffBreakdownYearDto, final TimeOffBalancePojo balancePojo) {
    if (balancePojo.getAppliedAccumulation() == null) {
      balancePojo.setAppliedAccumulation(0);
    }

    Integer newAppliedAccumulation =
        balancePojo.getAppliedAccumulation() + timeOffBreakdownYearDto.getAccrualHours();
    balancePojo.setAppliedAccumulation(newAppliedAccumulation);

    if (balancePojo.reachMaxBalance(true)) {
      newAppliedAccumulation = balancePojo.getMaxBalance() - balancePojo.getBalance();
      balancePojo.setAppliedAccumulation(newAppliedAccumulation);
    }

    LocalDate currentDate = LocalDate.MIN;
    currentDate = currentDate.withYear(timeOffBreakdownYearDto.getYear());
    final LocalDateTime localDateTime = LocalDateTime.of(currentDate, LocalTime.MIN);

    final TimeOffBreakdownItemDto timeOffBreakdownItemDto =
        TimeOffBreakdownItemDto.builder()
            .date(localDateTime)
            .detail(TIME_OFF_ACCRUED)
            .amount(timeOffBreakdownYearDto.getAccrualHours())
            .balance(balancePojo.getBalance() + balancePojo.getAppliedAccumulation())
            .breakdownType(TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_ACCRUAL)
            .build();
    resultTimeOffBreakdownItemList.add(timeOffBreakdownItemDto);
  }

  private List<Integer> getValidYearPeriod(final Timestamp createdAt, final Timestamp expiredAt,
      final LocalDateTime selectedDate) {
    final LocalDateTime createDate = DateUtil.toLocalDateTime(createdAt);
    final LocalDateTime expireDate = expiredAt == null ? selectedDate
        : DateUtil.toLocalDateTime(expiredAt);

    Integer startYear = createDate.getYear();
    final Integer endYear = expireDate.getYear();

    if (startYear > endYear) {
      return Collections.emptyList();
    }

    final List<Integer> validYear = new ArrayList<>();
    while (startYear < endYear) {
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
