package shamu.company.timeoff.service.impl;

import static shamu.company.timeoff.service.impl.TimeOffAccrualServiceImpl.invalidByStartDateAndEndDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffRequestDateDto;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffAdjustment;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.TimeOffAdjustmentRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.user.entity.User;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffDetailServiceImpl implements TimeOffDetailService {

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;
  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;
  private final TimeOffRequestDateRepository timeOffRequestDateRepository;
  private final TimeOffAdjustmentRepository timeOffAdjustmentRepository;
  private final TimeOffAccrualNatureStrategyServiceImpl accrualNatureStrategyService;
  private final TimeOffAccrualAnniversaryStrategyServiceImpl accrualAnniversaryStrategyService;
  private final TimeOffAccrualMonthStrategyServiceImpl accrualMonthStrategyService;

  @Autowired
  public TimeOffDetailServiceImpl(
      TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      TimeOffRequestDateRepository timeOffRequestDateRepository,
      TimeOffAdjustmentRepository timeOffAdjustmentRepository,
      TimeOffAccrualNatureStrategyServiceImpl accrualNatureStrategyService,
      TimeOffAccrualAnniversaryStrategyServiceImpl accrualAnniversaryStrategyService,
      TimeOffAccrualMonthStrategyServiceImpl accrualMonthStrategyService
  ) {
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
    this.timeOffAdjustmentRepository = timeOffAdjustmentRepository;
    this.accrualNatureStrategyService = accrualNatureStrategyService;
    this.accrualAnniversaryStrategyService = accrualAnniversaryStrategyService;
    this.accrualMonthStrategyService = accrualMonthStrategyService;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(Long policyUserId, LocalDateTime endDateTime) {
    Optional<TimeOffPolicyUser> timeOffPolicyUserContainer = timeOffPolicyUserRepository
        .findById(policyUserId);

    if (!timeOffPolicyUserContainer.isPresent()) {
      return null;
    }

    TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserContainer.get();
    TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();

    if (BooleanUtils.isFalse(timeOffPolicy.getIsLimited())) {
      return getUnlimitedTimeOffBreakdown(timeOffPolicyUser, endDateTime);
    }

    return getLimitedTimeOffBreakdown(timeOffPolicyUser, endDateTime);
  }

  private TimeOffBreakdownDto getUnlimitedTimeOffBreakdown(TimeOffPolicyUser timeOffPolicyUser,
      LocalDateTime endDateTime) {

    User user = timeOffPolicyUser.getUser();
    TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();
    List<TimeOffRequestDateDto> timeOffRequestDateDtoList =
        timeOffRequestDateRepository
            .getNoRejectedRequestOffByUserIdAndPolicyId(user.getId(),timeOffPolicy.getId());
    List<TimeOffBreakdownItemDto> requestDateBreakdownList = getBreakdownListFromRequestOff(
        timeOffRequestDateDtoList);

    TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setShowBalance(false);
    ZonedDateTime zonedDateTime = endDateTime.atZone(ZoneId.of("UTC"));
    timeOffBreakdownDto.setUntilDateInMillis(zonedDateTime.toEpochSecond() * 1000);

    requestDateBreakdownList.sort(Comparator.comparing(TimeOffBreakdownItemDto::getDate));

    List<TimeOffBreakdownItemDto> breakdownItemDtos = requestDateBreakdownList.stream()
        .filter(breakdown -> breakdown.getDate().isBefore(endDateTime))
        .collect(Collectors.toList());
    timeOffBreakdownDto.setList(breakdownItemDtos);
    return timeOffBreakdownDto;
  }

  private TimeOffBreakdownDto getLimitedTimeOffBreakdown(TimeOffPolicyUser timeOffPolicyUser,
      LocalDateTime endDateTime) {
    List<TimeOffPolicyAccrualSchedule> timeOffPolicyScheduleList =
        timeOffPolicyAccrualScheduleRepository
            .findAllWithExpiredTimeOffPolicy(timeOffPolicyUser.getTimeOffPolicy());

    if (CollectionUtils.isEmpty(timeOffPolicyScheduleList)) {
      return null;
    }

    TimeOffAccrualFrequency timeOffFrequency = timeOffPolicyScheduleList.get(0)
        .getTimeOffAccrualFrequency();
    Long timeOffFrequencyId = timeOffFrequency.getId();

    List<TimeOffPolicyAccrualSchedule> trimmedScheduleList = trimTimeOffPolicyScheduleList(
        timeOffPolicyScheduleList, timeOffPolicyUser.getUser());
    List<TimeOffBreakdownItemDto> balanceAdjustment = getBalanceAdjustmentList(timeOffPolicyUser);

    TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo =
        TimeOffBreakdownCalculatePojo.builder()
            .trimmedScheduleList(trimmedScheduleList)
            .balanceAdjustment(balanceAdjustment)
            .policyUser(timeOffPolicyUser)
            .untilDate(endDateTime)
            .build();

    return getTimeOffBreakdownByFrequency(timeOffFrequencyId, timeOffBreakdownCalculatePojo);
  }

  private TimeOffBreakdownDto getTimeOffBreakdownByFrequency(Long timeOffFrequencyId,
      TimeOffBreakdownCalculatePojo calculatePojo) {

    TimeOffBreakdownItemDto startingBreakdown = TimeOffBreakdownItemDto
        .fromTimeOffPolicyUser(calculatePojo.getPolicyUser());

    TimeOffBreakdownDto resultTimeOffBreakdownDto = null;

    if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE
        .equalsTo(timeOffFrequencyId)) {

      resultTimeOffBreakdownDto =
          accrualNatureStrategyService.getTimeOffBreakdown(startingBreakdown, calculatePojo);
    } else if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO
        .equalsTo(timeOffFrequencyId)) {

      resultTimeOffBreakdownDto =
          accrualAnniversaryStrategyService.getTimeOffBreakdown(startingBreakdown, calculatePojo);
    } else if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_THREE
        .equalsTo(timeOffFrequencyId)) {

      resultTimeOffBreakdownDto =
          accrualMonthStrategyService.getTimeOffBreakdown(startingBreakdown, calculatePojo);
    }

    if (resultTimeOffBreakdownDto != null) {
      postProcessOfTimeOffBreakdown(resultTimeOffBreakdownDto, timeOffFrequencyId, calculatePojo);
    }
    return resultTimeOffBreakdownDto;
  }

  private void postProcessOfTimeOffBreakdown(TimeOffBreakdownDto timeOffBreakdownDto,
      Long frequencyTypeId, TimeOffBreakdownCalculatePojo calculatePojo) {

    List<TimeOffBreakdownItemDto> timeOffBreakdownItemList = timeOffBreakdownDto.getList();
    Iterator<TimeOffBreakdownItemDto> breakdownItemListIterator = timeOffBreakdownItemList
        .iterator();
    breakdownItemListIterator.next();

    while (breakdownItemListIterator.hasNext()) {
      TimeOffBreakdownItemDto timeOffBreakdownItem = breakdownItemListIterator.next();

      if (TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_ACCRUAL
          .equals(timeOffBreakdownItem.getBreakdownType())) {

        if (TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE
            .equalsTo(frequencyTypeId)
            || TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO
            .equalsTo(frequencyTypeId)) {
          timeOffBreakdownItem.setDate(timeOffBreakdownItem.getDate().plusYears(1));
        } else {
          timeOffBreakdownItem.setDate(timeOffBreakdownItem.getDate().plusMonths(1));
        }
      }
    }

    List<TimeOffBreakdownItemDto> newTimeOffBreakdownItemList = timeOffBreakdownItemList.stream()
        .filter((timeOffBreakdownItemDto -> timeOffBreakdownItemDto.getDate()
            .isBefore(calculatePojo.getUntilDate())))
        .collect(Collectors.toList());
    timeOffBreakdownDto.setList(newTimeOffBreakdownItemList);
    timeOffBreakdownDto.resetBalance();
    timeOffBreakdownDto.setShowBalance(true);

    ZonedDateTime zonedDateTime = calculatePojo.getUntilDate().atZone(ZoneId.of("UTC"));
    timeOffBreakdownDto.setUntilDateInMillis(zonedDateTime.toEpochSecond() * 1000);
  }

  private List<TimeOffBreakdownItemDto> getBalanceAdjustmentList(
      TimeOffPolicyUser timeOffPolicyUser) {
    User user = timeOffPolicyUser.getUser();
    TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();

    List<TimeOffRequestDateDto> timeOffRequestDateDtoList =
        timeOffRequestDateRepository
            .getNoRejectedRequestOffByUserIdAndPolicyId(user.getId(), timeOffPolicy.getId());

    List<TimeOffBreakdownItemDto> requestDateBreakdownList = getBreakdownListFromRequestOff(
        timeOffRequestDateDtoList);

    List<TimeOffAdjustment> timeOffAdjustmentList = timeOffAdjustmentRepository
        .findAllByUserIdAndAndTimeOffPolicyId(user.getId(), timeOffPolicy.getId());
    List<TimeOffBreakdownItemDto> adjustmentBreakdownList = getBreakdownListFromAdjustment(
        timeOffAdjustmentList);

    List<TimeOffBreakdownItemDto> adjustmentList = new ArrayList<>(requestDateBreakdownList);
    adjustmentList.addAll(adjustmentBreakdownList);
    return adjustmentList;
  }

  private void populateBreakdownItem(
      LinkedList<TimeOffBreakdownItemDto> breakdownItemList,
      TimeOffBreakdownItemDto timeOffBreakdownItemDto,
      LocalDateTime startDate, LocalDateTime endDate) {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu");
    DateTimeFormatter currentYearFormatter = DateTimeFormatter.ofPattern("d MMM");

    String startDateString = startDate.getYear() == LocalDate.now().getYear()
        ? startDate.format(currentYearFormatter) : startDate.format(timeFormatter);
    String endDateString = endDate.getYear() == LocalDate.now().getYear()
        ? endDate.format(currentYearFormatter) : endDate.format(timeFormatter);

    String detailMessage =
        String.format("Time Off Requested:%s - %s", startDateString, endDateString);
    timeOffBreakdownItemDto.setDetail(detailMessage);
    timeOffBreakdownItemDto
        .setBreakdownType(TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_REQUEST);
    breakdownItemList.push(timeOffBreakdownItemDto);
  }

  private List<TimeOffBreakdownItemDto> getBreakdownListFromRequestOff(
      List<TimeOffRequestDateDto> timeOffRequestDateDtoList) {
    LocalDateTime startDate = null;
    Integer spanDays = null;
    Integer totalHours = null;

    LinkedList<TimeOffBreakdownItemDto> breakdownItemList = new LinkedList<>();

    for (TimeOffRequestDateDto timeOffRequestDateDto : timeOffRequestDateDtoList) {
      LocalDateTime currentDate = DateUtil.toLocalDateTime(timeOffRequestDateDto.getDate());

      if (startDate == null || !startDate.plusDays(spanDays).isEqual(currentDate)) {
        startDate = currentDate;
        spanDays = 1;
        totalHours = timeOffRequestDateDto.getHours();

        TimeOffBreakdownItemDto timeOffBreakdownItemDto = new TimeOffBreakdownItemDto();
        timeOffBreakdownItemDto.setAmount(-totalHours);
        timeOffBreakdownItemDto.setDate(startDate);

        populateBreakdownItem(breakdownItemList, timeOffBreakdownItemDto, startDate, currentDate);
      } else if (startDate.plusDays(spanDays).isEqual(currentDate)) {
        spanDays += 1;
        totalHours += timeOffRequestDateDto.getHours();

        TimeOffBreakdownItemDto timeOffBreakdownItemDto = breakdownItemList.pop();
        timeOffBreakdownItemDto.setAmount(-totalHours);
        timeOffBreakdownItemDto.setDate(startDate);

        populateBreakdownItem(breakdownItemList, timeOffBreakdownItemDto, startDate, currentDate);
      }

    }
    return breakdownItemList;
  }

  private List<TimeOffBreakdownItemDto> getBreakdownListFromAdjustment(
      List<TimeOffAdjustment> timeOffAdjustmentList) {
    return timeOffAdjustmentList.stream().map(TimeOffBreakdownItemDto::fromTimeOffAdjustment)
        .collect(Collectors.toList());
  }

  private List<TimeOffPolicyAccrualSchedule> trimTimeOffPolicyScheduleList(
      List<TimeOffPolicyAccrualSchedule> timeOffPolicyAccrualScheduleList, User user) {

    List<TimeOffPolicyAccrualSchedule> trimmedScheduleList = new ArrayList<>();
    for (TimeOffPolicyAccrualSchedule accrualSchedule : timeOffPolicyAccrualScheduleList) {

      LocalDateTime userEnrollDateTime = DateUtil.toLocalDateTime(user.getCreatedAt());
      Long frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();
      if (accrualSchedule.getExpiredAt() == null) {
        trimmedScheduleList.add(accrualSchedule);
        continue;
      }

      if (invalidByStartDateAndEndDate(accrualSchedule.getCreatedAt(),
          accrualSchedule.getExpiredAt(), userEnrollDateTime, frequencyId)) {
        continue;
      }

      trimmedScheduleList.add(accrualSchedule);
    }

    return trimmedScheduleList;
  }
}
