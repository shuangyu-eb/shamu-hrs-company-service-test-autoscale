package shamu.company.timeoff.service.impl;

import static shamu.company.timeoff.service.impl.TimeOffAccrualServiceImpl.invalidByStartDateAndEndDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffAdjustmentPojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.pojo.TimeOffRequestDatePojo;
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
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      final TimeOffRequestDateRepository timeOffRequestDateRepository,
      final TimeOffAdjustmentRepository timeOffAdjustmentRepository,
      final TimeOffAccrualNatureStrategyServiceImpl accrualNatureStrategyService,
      final TimeOffAccrualAnniversaryStrategyServiceImpl accrualAnniversaryStrategyService,
      final TimeOffAccrualMonthStrategyServiceImpl accrualMonthStrategyService
  ) {
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
    this.timeOffAdjustmentRepository = timeOffAdjustmentRepository;
    this.accrualNatureStrategyService = accrualNatureStrategyService;
    this.accrualAnniversaryStrategyService = accrualAnniversaryStrategyService;
    this.accrualMonthStrategyService = accrualMonthStrategyService;
  }

  @Override
  public TimeOffBreakdownDto getTimeOffBreakdown(
      final Long policyUserId, final LocalDateTime endDateTime) {
    final Optional<TimeOffPolicyUser> timeOffPolicyUserContainer = timeOffPolicyUserRepository
        .findById(policyUserId);

    if (!timeOffPolicyUserContainer.isPresent()) {
      return null;
    }

    final TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserContainer.get();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();

    if (BooleanUtils.isFalse(timeOffPolicy.getIsLimited())) {
      return getUnlimitedTimeOffBreakdown(timeOffPolicyUser, endDateTime);
    }

    return getLimitedTimeOffBreakdown(timeOffPolicyUser, endDateTime);
  }

  private TimeOffBreakdownDto getUnlimitedTimeOffBreakdown(
      final TimeOffPolicyUser timeOffPolicyUser,
      final LocalDateTime endDateTime) {

    final User user = timeOffPolicyUser.getUser();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();
    final List<TimeOffRequestDatePojo> timeOffRequestDatePojos =
        timeOffRequestDateRepository
            .getNoRejectedRequestOffByUserIdAndPolicyId(user.getId(), timeOffPolicy.getId());
    final List<TimeOffBreakdownItemDto> requestDateBreakdownList = getBreakdownListFromRequestOff(
        timeOffRequestDatePojos);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setShowBalance(false);
    final ZonedDateTime zonedDateTime = endDateTime.atZone(ZoneId.of("UTC"));
    timeOffBreakdownDto.setUntilDateInMillis(zonedDateTime.toEpochSecond() * 1000);

    requestDateBreakdownList.sort(Comparator.comparing(TimeOffBreakdownItemDto::getDate));

    final List<TimeOffBreakdownItemDto> breakdownItemDtos = requestDateBreakdownList.stream()
        .filter(breakdown -> breakdown.getDate().isBefore(endDateTime))
        .collect(Collectors.toList());
    timeOffBreakdownDto.setList(breakdownItemDtos);
    return timeOffBreakdownDto;
  }

  private TimeOffBreakdownDto getLimitedTimeOffBreakdown(final TimeOffPolicyUser timeOffPolicyUser,
      final LocalDateTime endDateTime) {
    final List<TimeOffPolicyAccrualSchedule> timeOffPolicyScheduleList =
        timeOffPolicyAccrualScheduleRepository
            .findAllWithExpiredTimeOffPolicy(timeOffPolicyUser.getTimeOffPolicy());

    if (CollectionUtils.isEmpty(timeOffPolicyScheduleList)) {
      return null;
    }

    final TimeOffAccrualFrequency timeOffFrequency = timeOffPolicyScheduleList.get(0)
        .getTimeOffAccrualFrequency();
    final Long timeOffFrequencyId = timeOffFrequency.getId();

    final List<TimeOffPolicyAccrualSchedule> trimmedScheduleList = trimTimeOffPolicyScheduleList(
        timeOffPolicyScheduleList, timeOffPolicyUser.getUser());
    final List<TimeOffBreakdownItemDto> balanceAdjustment =
        getBalanceAdjustmentList(timeOffPolicyUser);

    final TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo =
        TimeOffBreakdownCalculatePojo.builder()
            .trimmedScheduleList(trimmedScheduleList)
            .balanceAdjustment(balanceAdjustment)
            .policyUser(timeOffPolicyUser)
            .untilDate(endDateTime)
            .build();

    return getTimeOffBreakdownByFrequency(timeOffFrequencyId, timeOffBreakdownCalculatePojo);
  }

  private TimeOffBreakdownDto getTimeOffBreakdownByFrequency(final Long timeOffFrequencyId,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    final TimeOffBreakdownItemDto startingBreakdown = TimeOffBreakdownItemDto
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
      postProcessOfTimeOffBreakdown(resultTimeOffBreakdownDto, calculatePojo);
    }
    return resultTimeOffBreakdownDto;
  }

  private void postProcessOfTimeOffBreakdown(final TimeOffBreakdownDto timeOffBreakdownDto,
      final TimeOffBreakdownCalculatePojo calculatePojo) {

    final List<TimeOffBreakdownItemDto> timeOffBreakdownItemList = timeOffBreakdownDto.getList();

    final List<TimeOffBreakdownItemDto> newTimeOffBreakdownItemList = timeOffBreakdownItemList
        .stream()
        .filter((timeOffBreakdownItemDto -> timeOffBreakdownItemDto.getDate()
            .isBefore(calculatePojo.getUntilDate())))
        .collect(Collectors.toList());
    timeOffBreakdownDto.setList(newTimeOffBreakdownItemList);
    timeOffBreakdownDto.resetBalance();
    timeOffBreakdownDto.setShowBalance(true);

    final ZonedDateTime zonedDateTime = calculatePojo.getUntilDate().atZone(ZoneId.of("UTC"));
    timeOffBreakdownDto.setUntilDateInMillis(zonedDateTime.toEpochSecond() * 1000);
  }

  private List<TimeOffBreakdownItemDto> getBalanceAdjustmentList(
      final TimeOffPolicyUser timeOffPolicyUser) {
    final User user = timeOffPolicyUser.getUser();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();

    final List<TimeOffRequestDatePojo> timeOffRequestDatePojos =
        timeOffRequestDateRepository
            .getNoRejectedRequestOffByUserIdAndPolicyId(user.getId(), timeOffPolicy.getId());

    final List<TimeOffBreakdownItemDto> requestDateBreakdownList = getBreakdownListFromRequestOff(
        timeOffRequestDatePojos);

    final List<TimeOffAdjustmentPojo> timeOffAdjustmentPojos = timeOffAdjustmentRepository
        .findAllByUserIdAndTimeOffPolicyId(user.getId(), timeOffPolicy.getId());
    final List<TimeOffBreakdownItemDto> adjustmentBreakdownList = getBreakdownListFromAdjustment(
        timeOffAdjustmentPojos);

    final List<TimeOffBreakdownItemDto> adjustmentList = new ArrayList<>(requestDateBreakdownList);
    adjustmentList.addAll(adjustmentBreakdownList);

    adjustmentList.sort(Comparator.comparing(TimeOffBreakdownItemDto::getDate));
    return adjustmentList;
  }

  private void populateBreakdownItem(
      final LinkedList<TimeOffBreakdownItemDto> breakdownItemList,
      final TimeOffBreakdownItemDto timeOffBreakdownItemDto,
      final LocalDateTime startDate, final LocalDateTime endDate) {
    final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu");
    final DateTimeFormatter currentYearFormatter = DateTimeFormatter.ofPattern("d MMM");

    final String startDateString = startDate.getYear() == LocalDate.now().getYear()
        ? startDate.format(currentYearFormatter) : startDate.format(timeFormatter);
    final String endDateString = endDate.getYear() == LocalDate.now().getYear()
        ? endDate.format(currentYearFormatter) : endDate.format(timeFormatter);

    final String detailMessage =
        String.format("Time Off Requested:%s - %s", startDateString, endDateString);
    timeOffBreakdownItemDto.setDetail(detailMessage);
    timeOffBreakdownItemDto
        .setBreakdownType(TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_REQUEST);
    breakdownItemList.push(timeOffBreakdownItemDto);
  }

  private List<TimeOffBreakdownItemDto> getBreakdownListFromRequestOff(
      final List<TimeOffRequestDatePojo> timeOffRequestDatePojos) {

    final LinkedList<TimeOffBreakdownItemDto> breakdownItemList = new LinkedList<>();
    for (final TimeOffRequestDatePojo timeOffRequestDatePojo : timeOffRequestDatePojos) {
      final TimeOffBreakdownItemDto timeOffBreakdownItemDto = TimeOffBreakdownItemDto.builder()
          .amount(-timeOffRequestDatePojo.getHours())
          .date(DateUtil.toLocalDateTime(timeOffRequestDatePojo.getCreateDate()))
          .build();

      populateBreakdownItem(breakdownItemList, timeOffBreakdownItemDto,
          DateUtil.toLocalDateTime(timeOffRequestDatePojo.getStartDate()),
          DateUtil.toLocalDateTime(timeOffRequestDatePojo.getEndDate()));
    }
    return breakdownItemList;
  }

  private List<TimeOffBreakdownItemDto> getBreakdownListFromAdjustment(
      final List<TimeOffAdjustmentPojo> timeOffAdjustmentPojos) {
    return timeOffAdjustmentPojos.stream().map(TimeOffBreakdownItemDto::fromTimeOffAdjustment)
        .collect(Collectors.toList());
  }

  private List<TimeOffPolicyAccrualSchedule> trimTimeOffPolicyScheduleList(
      final List<TimeOffPolicyAccrualSchedule> timeOffPolicyAccrualScheduleList, final User user) {

    final List<TimeOffPolicyAccrualSchedule> trimmedScheduleList = new ArrayList<>();
    for (final TimeOffPolicyAccrualSchedule accrualSchedule : timeOffPolicyAccrualScheduleList) {

      final LocalDateTime userEnrollDateTime = DateUtil.toLocalDateTime(user.getCreatedAt());
      final Long frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();
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
