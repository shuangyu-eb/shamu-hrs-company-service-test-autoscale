package shamu.company.timeoff.service.impl;

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
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.user.entity.User;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffAccrualNatureStrategyServiceImpl extends TimeOffAccrualServiceImpl {

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Autowired
  public TimeOffAccrualNatureStrategyServiceImpl(
      AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository) {
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(TimeOffBreakdownItemDto startingBreakdown,
      TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList = getAccrualDataByYear(
        calculatePojo.getTrimmedScheduleList(), calculatePojo.getPolicyUser().getUser());
    timeOffBreakdownYearDtoList = addMissingYearDto(timeOffBreakdownYearDtoList);

    timeOffBreakdownYearDtoList.forEach(timeOffBreakdownYearDto -> {
      timeOffBreakdownYearDto.setYear(timeOffBreakdownYearDto.getYear() + 1);
    });

    return getFinalTimeOffBreakdown(timeOffBreakdownYearDtoList,
        startingBreakdown, calculatePojo.getBalanceAdjustment());
  }

  private List<TimeOffBreakdownYearDto> getAccrualDataByYear(
      List<TimeOffPolicyAccrualSchedule> trimmedScheduleList, User user) {
    LocalDateTime userJoinDateTime = DateUtil.toLocalDateTime(user.getCreatedAt());

    HashMap<Integer, TimeOffBreakdownYearDto> accrualDate = new HashMap<>();
    trimmedScheduleList.forEach(accrualSchedule -> {

      List<Timestamp> validStartAndEndDate =
          getValidAccrualScheduleStartAndEndDate(userJoinDateTime, accrualSchedule);
      List<Integer> validYears =
          getValidYearPeriod(validStartAndEndDate.get(0), validStartAndEndDate.get(1));

      Map breakdownYearMap = getAccrualBreakdownYearMap(validYears, accrualSchedule);
      accrualDate.putAll(breakdownYearMap);

      List<AccrualScheduleMilestone> accrualScheduleMilestoneList =
          accrualScheduleMilestoneRepository
              .findByTimeOffPolicyAccrualScheduleId(accrualSchedule.getId());
      accrualScheduleMilestoneList = trimTimeOffPolicyScheduleMilestones(
          accrualScheduleMilestoneList, user, accrualSchedule);

      // sort milestones
      accrualScheduleMilestoneList.sort(Comparator.comparing(BaseEntity::getCreatedAt));

      accrualScheduleMilestoneList.forEach(accrualScheduleMilestone -> {
        List<Timestamp> validMilestoneStartAndEndDate =
            getValidMilestoneStartAndEndDate(userJoinDateTime, accrualScheduleMilestone);
        List<Integer> validMilestoneYears = getValidYearPeriod(
            validMilestoneStartAndEndDate.get(0),
            validMilestoneStartAndEndDate.get(1)
        );
        Map milestoneBreakdownYearMap =
            getAccrualBreakdownYearMap(validMilestoneYears, accrualScheduleMilestone);
        accrualDate.putAll(milestoneBreakdownYearMap);
      });
    });
    return new ArrayList<>(accrualDate.values());
  }

  private LinkedList<TimeOffBreakdownYearDto> addMissingYearDto(
      List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtos) {

    LinkedList<TimeOffBreakdownYearDto> newTimeOffBreakdownList = new LinkedList<>();

    Integer previousYear = null;
    for (TimeOffBreakdownYearDto timeOffBreakdownYearDto : timeOffBreakdownYearDtos) {
      Integer currentYear = timeOffBreakdownYearDto.getYear();

      while (previousYear != null && currentYear > previousYear + 1) {

        TimeOffBreakdownYearDto previousYearDto = newTimeOffBreakdownList.peekLast();
        TimeOffBreakdownYearDto newTimeOffYearDto = new TimeOffBreakdownYearDto();
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
      List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList,
      TimeOffBreakdownItemDto startingBreakdown,
      List<TimeOffBreakdownItemDto> balanceAdjustmentList) {

    timeOffBreakdownYearDtoList.sort(Comparator.comparingInt(TimeOffBreakdownYearDto::getYear));

    TimeOffBalancePojo balancePojo =
        new TimeOffBalancePojo(startingBreakdown.getBalance(), 0);

    LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    resultTimeOffBreakdownItemList.add(startingBreakdown);

    for (TimeOffBreakdownYearDto timeOffBreakdownYearDto : timeOffBreakdownYearDtoList) {
      // apply carryoverLimit
      TimeOffBreakdownItemDto lastTimeOffBreakdown = resultTimeOffBreakdownItemList.peekLast();

      LocalDate untilDate = LocalDate.MIN.withYear(timeOffBreakdownYearDto.getYear());
      LocalDateTime untilDateTime = LocalDateTime.of(untilDate, LocalTime.MIN);
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

    TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  private void populateBreakdownListFromYearDto(
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList,
      TimeOffBreakdownYearDto timeOffBreakdownYearDto, TimeOffBalancePojo balancePojo) {
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
    LocalDateTime localDateTime = LocalDateTime.of(currentDate, LocalTime.MIN);

    TimeOffBreakdownItemDto timeOffBreakdownItemDto =
        TimeOffBreakdownItemDto.builder()
            .date(localDateTime)
            .detail(TIME_OFF_ACCRUED)
            .amount(timeOffBreakdownYearDto.getAccrualHours())
            .balance(balancePojo.getBalance() + balancePojo.getAppliedAccumulation())
            .breakdownType(TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_ACCRUAL)
            .build();
    resultTimeOffBreakdownItemList.add(timeOffBreakdownItemDto);
  }

  private List<Integer> getValidYearPeriod(Timestamp createdAt, Timestamp expiredAt) {
    LocalDateTime createDate = DateUtil.toLocalDateTime(createdAt);
    LocalDateTime expireDate = expiredAt == null ? LocalDateTime.now()
        : DateUtil.toLocalDateTime(expiredAt);

    Integer startYear = createDate.getYear();
    Integer endYear = expireDate.getYear();

    if (startYear > endYear) {
      return Collections.emptyList();
    }

    List<Integer> validYear = new ArrayList<>();
    while (startYear < endYear) {
      validYear.add(startYear++);
    }
    return validYear;
  }

  private <T> Map<Integer, TimeOffBreakdownItemDto> getAccrualBreakdownYearMap(
      List<Integer> validYears, T t) {
    HashMap breakdownYear = new HashMap();

    for (Integer currentYear : validYears) {
      TimeOffBreakdownYearDto timeOffBreakdownYearDto = transferToTimeOffYearDto(t);
      timeOffBreakdownYearDto.setYear(currentYear);
      breakdownYear.put(currentYear, t);
    }
    return breakdownYear;
  }

  private <T> TimeOffBreakdownYearDto transferToTimeOffYearDto(T t) {
    TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    BeanUtils.copyProperties(t, timeOffBreakdownYearDto);
    return timeOffBreakdownYearDto;
  }

}
