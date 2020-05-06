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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import shamu.company.job.entity.JobUser;
import shamu.company.job.service.JobUserService;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownMonthDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffAccrualMonthStrategyService extends TimeOffAccrualService {

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  private final JobUserService jobUserService;

  @Autowired
  public TimeOffAccrualMonthStrategyService(
      final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository,
      @Lazy final JobUserService jobUserService) {
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
    this.jobUserService = jobUserService;
  }

  @Override
  TimeOffBreakdownDto getTimeOffBreakdownInternal(
      final TimeOffBreakdownYearDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {
    final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto = new TimeOffBreakdownMonthDto();
    timeOffBreakdownMonthDto.setYearData(startingBreakdown);
    BeanUtils.copyProperties(startingBreakdown, timeOffBreakdownMonthDto);

    return getTimeOffBreakdown(timeOffBreakdownMonthDto, calculatePojo);
  }

  TimeOffBreakdownDto getTimeOffBreakdown(
      final TimeOffBreakdownMonthDto startingBreakdown,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList =
        getAccrualDataByMonth(calculatePojo);
    timeOffBreakdownMonthDtoList = addMissingMonthDto(timeOffBreakdownMonthDtoList);

    // calculate the month which dividing day reside
    // refer to
    // https://tardisone.atlassian.net/browse/SH-689?focusedCommentId=28173&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-28173
    resetDividingMonths(timeOffBreakdownMonthDtoList, startingBreakdown.getDate());

    timeOffBreakdownMonthDtoList.forEach(
        timeOffBreakdownMonthDto ->
            timeOffBreakdownMonthDto.setDate(
                timeOffBreakdownMonthDto.getDate().withDayOfMonth(1).plusMonths(1)));

    timeOffBreakdownMonthDtoList.add(0, startingBreakdown);

    return getFinalMonthBreakdown(
        timeOffBreakdownMonthDtoList, calculatePojo.getBalanceAdjustment());
  }

  private void resetDividingMonths(
      final List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList,
      final LocalDate dividingDay) {
    final LocalDate[] currentDividingDay = new LocalDate[] {LocalDate.from(dividingDay)};
    currentDividingDay[0] = currentDividingDay[0].plusMonths(12);

    final TimeOffBreakdownMonthDto[] currentMonthDto = {null};
    final TimeOffBreakdownMonthDto[] previousMonthDto = {null};

    timeOffBreakdownMonthDtoList.forEach(
        timeOffBreakdownMonthDto -> {
          previousMonthDto[0] = currentMonthDto[0];
          currentMonthDto[0] = timeOffBreakdownMonthDto;

          if (timeOffBreakdownMonthDto.getDate().getYear() != dividingDay.getYear()
              && timeOffBreakdownMonthDto.getDate().getMonthValue()
              == dividingDay.getMonthValue()) {
            resetDividingMonth(currentMonthDto[0], previousMonthDto[0], dividingDay);
          }
        });
  }

  private void resetDividingMonth(
      final TimeOffBreakdownMonthDto dividingMonth,
      final TimeOffBreakdownMonthDto previousMonth,
      final LocalDate dividingDay) {
    final int dayOfMonth = dividingDay.getDayOfMonth();
    final int totalDays =
        dividingMonth.getDate().getMonth().length(Year.isLeap(dividingMonth.getDate().getYear()));

    final double newBalance =
        previousMonth.getAccrualHours() * ((double) dayOfMonth / totalDays)
            + dividingMonth.getAccrualHours() * (1d - (double) dayOfMonth / totalDays);

    dividingMonth.setAccrualHours((int) Math.round(newBalance));
    previousMonth.setLastMonthOfPreviousAnniversaryYear(true);
  }

  private List<TimeOffBreakdownMonthDto> getAccrualDataByMonth(
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    final JobUser targetUser =
        jobUserService.getJobUserByUserId(calculatePojo.getPolicyUser().getUser().getId());
    final LocalDate userHiredDate = DateUtil.fromTimestamp(targetUser.getStartDate());

    final LocalDateTime userJoinPolicyDate =
        DateUtil.toLocalDateTime(calculatePojo.getPolicyUser().getCreatedAt());

    final TreeMap<Integer, TreeMap<Integer, TimeOffBreakdownMonthDto>> accrualDate =
        new TreeMap<>();

    calculatePojo
        .getTrimmedScheduleList()
        .forEach(
            accrualSchedule -> {
              final LocalDate scheduleBaseTime =
                  getScheduleStartBaseTime(
                      userHiredDate, userJoinPolicyDate.toLocalDate(), accrualSchedule);
              final List<LocalDate> validStartAndEndDate =
                  getValidScheduleOrMilestonePeriod(
                      scheduleBaseTime,
                      accrualSchedule.getCreatedAt(),
                      accrualSchedule.getExpiredAt());

              final List<LocalDate> validPeriod =
                  getValidMonthPeriod(
                      validStartAndEndDate.get(0),
                      validStartAndEndDate.get(1),
                      calculatePojo.getUntilDate());

              addToResultMonthMap(
                  accrualDate, validPeriod, accrualSchedule, userJoinPolicyDate.toLocalDate());

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
                    final LocalDateTime milestoneStartTime =
                        userJoinPolicyDate.plusYears(accrualScheduleMilestone.getAnniversaryYear());
                    final List<LocalDate> validMilestoneStartAndEndDate =
                        getValidScheduleOrMilestonePeriod(
                            milestoneStartTime.toLocalDate(),
                            accrualScheduleMilestone.getCreatedAt(),
                            accrualScheduleMilestone.getExpiredAt());

                    final List<LocalDate> validMilestonePeriod =
                        getValidMonthPeriod(
                            validMilestoneStartAndEndDate.get(0),
                            validMilestoneStartAndEndDate.get(1),
                            calculatePojo.getUntilDate());

                    final TimeOffPolicyAccrualSchedule tempAccrualSchedule =
                        new TimeOffPolicyAccrualSchedule();
                    BeanUtils.copyProperties(accrualScheduleMilestone, tempAccrualSchedule);
                    addToResultMonthMap(
                        accrualDate,
                        validMilestonePeriod,
                        tempAccrualSchedule,
                        milestoneStartTime.toLocalDate());
                  });
            });

    final List<TimeOffBreakdownMonthDto> monthList = new ArrayList<>();

    accrualDate.values().forEach(monthMap -> monthList.addAll(monthMap.values()));

    // recalculate first month's time off
    if (!monthList.isEmpty()) {
      final TimeOffBreakdownMonthDto firstMonth = monthList.get(0);
      final LocalDate startDate = firstMonth.getDate();
      final int dayOfMonth = startDate.getDayOfMonth();
      final int totalDays =
          Month.of(startDate.getMonthValue()).length(Year.isLeap(startDate.getYear()));
      final double newTimeOffTime =
          (double)
              Math.round(
                  firstMonth.getAccrualHours() * ((totalDays - dayOfMonth) / (double) totalDays));
      firstMonth.setAccrualHours((int) newTimeOffTime);
    }

    return monthList;
  }

  private List<LocalDate> getValidMonthPeriod(
      final LocalDate startDate, final LocalDate endDate, final LocalDate selectedDate) {

    LocalDate currentMonthDate = startDate;

    final LocalDate endDateTime = endDate == null ? selectedDate : endDate;

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
      final TimeOffPolicyAccrualSchedule accrualSchedule,
      LocalDate anniversaryBaseDate) {

    if (CollectionUtils.isEmpty(validPeriodList)) {
      return;
    }

    anniversaryBaseDate = anniversaryBaseDate.withDayOfMonth(1);
    for (final LocalDate currentTime : validPeriodList) {
      final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto =
          transferToMonthDto(accrualSchedule, currentTime, accrualSchedule.getAccrualHours() / 12);

      final boolean isParent =
          !anniversaryBaseDate.isAfter(currentTime)
              && anniversaryBaseDate.plusYears(1).isAfter(currentTime);

      timeOffBreakdownMonthDto.setHasParent(!isParent);

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

  private <T> TimeOffBreakdownMonthDto transferToMonthDto(
      final T t, final LocalDate currentTime, final Integer monthAccrualHours) {
    final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto =
        new TimeOffBreakdownMonthDto(currentTime, monthAccrualHours);

    final TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    timeOffBreakdownYearDto.setDate(currentTime);
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
      while (previousDate != null
          && (previousDate = previousDate.plusMonths(1))
          .isBefore(timeOffBreakdownMonthDto.getDate())) {

        final TimeOffBreakdownMonthDto newMonthBreakdown = new TimeOffBreakdownMonthDto();
        previousMonthDto = newTimeOffBreakdownList.peekLast();
        BeanUtils.copyProperties(previousMonthDto, newMonthBreakdown);
        newMonthBreakdown.setDate(previousDate);
        newMonthBreakdown.setHasParent(true);
        newTimeOffBreakdownList.add(newMonthBreakdown);
      }

      newTimeOffBreakdownList.add(timeOffBreakdownMonthDto);
      previousDate = timeOffBreakdownMonthDto.getDate();
    }

    return newTimeOffBreakdownList;
  }

  private TimeOffBreakdownDto getFinalMonthBreakdown(
      final List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtoList,
      final List<TimeOffBreakdownItemDto> balanceAdjustment) {

    final TimeOffBalancePojo balancePojo = new TimeOffBalancePojo(0);

    TimeOffBreakdownYearDto timeOffBreakdownYearDto;
    final LinkedList<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();

    for (final TimeOffBreakdownMonthDto timeOffBreakdownMonthDto : timeOffBreakdownMonthDtoList) {

      final LocalDate firstDateOfTheMonth = timeOffBreakdownMonthDto.getDate();
      // carryover
      populateBreakdownListFromCarryoverLimit(
          resultTimeOffBreakdownItemList, firstDateOfTheMonth, balancePojo);

      if (resultTimeOffBreakdownItemList.isEmpty() || !timeOffBreakdownMonthDto.isHasParent()) {

        final TimeOffBreakdownYearDto tmpYearDto = timeOffBreakdownMonthDto.getYearData();
        if (tmpYearDto != null) {
          balancePojo.setMaxBalance(tmpYearDto.getMaxBalance());
        }
      }

      // max balance
      populateBreakdownListFromMaxBalance(
          resultTimeOffBreakdownItemList, firstDateOfTheMonth, balancePojo);

      // accrual
      populateBreakdownListFromAccrualSchedule(
          resultTimeOffBreakdownItemList,
          firstDateOfTheMonth,
          timeOffBreakdownMonthDto.getAccrualHours(),
          balancePojo);

      // Adjustment
      populateBreakdownAdjustmentBefore(
          resultTimeOffBreakdownItemList,
          firstDateOfTheMonth.plusMonths(1),
          balanceAdjustment,
          balancePojo);

      timeOffBreakdownYearDto =
          timeOffBreakdownMonthDto.getLastMonthOfPreviousAnniversaryYear()
              ? timeOffBreakdownMonthDto.getYearData()
              : null;
      if (timeOffBreakdownYearDto != null) {
        balancePojo.setCarryOverLimit(timeOffBreakdownYearDto.getCarryoverLimit());
      } else {
        balancePojo.setCarryOverLimit(null);
      }
    }
    populateRemainingAdjustment(resultTimeOffBreakdownItemList, balanceAdjustment, balancePojo);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setBalance(balancePojo.getBalance());
    timeOffBreakdownDto.setList(resultTimeOffBreakdownItemList);
    return timeOffBreakdownDto;
  }

  @Override
  boolean support(final String frequencyType) {
    return AccrualFrequencyType.FREQUENCY_TYPE_THREE.equalsTo(frequencyType);
  }
}
