package shamu.company.timeoff.service;

import static shamu.company.timeoff.service.TimeOffAccrualService.invalidByStartDateAndEndDate;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
import shamu.company.user.entity.User;
import shamu.company.utils.DateUtil;

@Service
public class TimeOffDetailService {

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;
  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;
  private final TimeOffRequestDateRepository timeOffRequestDateRepository;
  private final TimeOffAdjustmentRepository timeOffAdjustmentRepository;
  private final TimeOffAccrualNatureStrategyService accrualNatureStrategyService;
  private final TimeOffAccrualAnniversaryStrategyService accrualAnniversaryStrategyService;
  private final TimeOffAccrualMonthStrategyService accrualMonthStrategyService;

  @Autowired
  public TimeOffDetailService(
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      final TimeOffRequestDateRepository timeOffRequestDateRepository,
      final TimeOffAdjustmentRepository timeOffAdjustmentRepository,
      final TimeOffAccrualNatureStrategyService accrualNatureStrategyService,
      final TimeOffAccrualAnniversaryStrategyService accrualAnniversaryStrategyService,
      final TimeOffAccrualMonthStrategyService accrualMonthStrategyService
  ) {
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
    this.timeOffAdjustmentRepository = timeOffAdjustmentRepository;
    this.accrualNatureStrategyService = accrualNatureStrategyService;
    this.accrualAnniversaryStrategyService = accrualAnniversaryStrategyService;
    this.accrualMonthStrategyService = accrualMonthStrategyService;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(
      final Long policyUserId, final LocalDate endDate) {
    final Optional<TimeOffPolicyUser> timeOffPolicyUserContainer = timeOffPolicyUserRepository
        .findById(policyUserId);

    if (!timeOffPolicyUserContainer.isPresent()) {
      return null;
    }

    final TimeOffPolicyUser timeOffPolicyUser = timeOffPolicyUserContainer.get();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();

    if (BooleanUtils.isFalse(timeOffPolicy.getIsLimited())) {
      return getUnlimitedTimeOffBreakdown(timeOffPolicyUser, endDate);
    }

    return getLimitedTimeOffBreakdown(timeOffPolicyUser, endDate);
  }

  private TimeOffBreakdownDto getUnlimitedTimeOffBreakdown(
      final TimeOffPolicyUser timeOffPolicyUser,
      final LocalDate endDate) {

    final User user = timeOffPolicyUser.getUser();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();
    final List<TimeOffRequestDatePojo> timeOffRequestDatePojos =
        timeOffRequestDateRepository
            .getTakenApprovedRequestOffByUserIdAndPolicyId(
                user.getId(), timeOffPolicy.getId(), DateUtil.getLocalUtcTime());
    final List<TimeOffBreakdownItemDto> requestDateBreakdownList = getBreakdownListFromRequestOff(
        timeOffRequestDatePojos);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setShowBalance(false);

    requestDateBreakdownList.sort(Comparator.comparing(TimeOffBreakdownItemDto::getDate));

    final List<TimeOffBreakdownItemDto> breakdownItemDtos = requestDateBreakdownList.stream()
        .filter(breakdown -> !breakdown.getDate().isAfter(endDate))
        .collect(Collectors.toList());
    timeOffBreakdownDto.setList(breakdownItemDtos);
    return timeOffBreakdownDto;
  }

  private TimeOffBreakdownDto getLimitedTimeOffBreakdown(final TimeOffPolicyUser timeOffPolicyUser,
      final LocalDate endDate) {
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

    // Sort schedule list
    trimmedScheduleList.sort(Comparator
        .comparing(TimeOffPolicyAccrualSchedule::getCreatedAt, Comparator.reverseOrder()));

    final List<TimeOffBreakdownItemDto> balanceAdjustment =
        getBalanceAdjustmentList(timeOffPolicyUser, endDate);

    final TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo =
        TimeOffBreakdownCalculatePojo.builder()
            .trimmedScheduleList(trimmedScheduleList)
            .balanceAdjustment(balanceAdjustment)
            .policyUser(timeOffPolicyUser)
            .untilDate(endDate)
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
        .filter((timeOffBreakdownItemDto -> !timeOffBreakdownItemDto.getDate()
            .isAfter(calculatePojo.getUntilDate())))
        .collect(Collectors.toList());
    timeOffBreakdownDto.setList(newTimeOffBreakdownItemList);
    timeOffBreakdownDto.resetBalance();
    timeOffBreakdownDto.setShowBalance(true);
  }

  private List<TimeOffBreakdownItemDto> getBalanceAdjustmentList(
      final TimeOffPolicyUser timeOffPolicyUser, final LocalDate endDate) {
    final User user = timeOffPolicyUser.getUser();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();

    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
    final List<TimeOffRequestDatePojo> timeOffRequestDatePojos =
        timeOffRequestDateRepository
            .getTakenApprovedRequestOffByUserIdAndPolicyId(
                user.getId(), timeOffPolicy.getId(), endDateTime);

    final List<TimeOffBreakdownItemDto> requestDateBreakdownList = getBreakdownListFromRequestOff(
        timeOffRequestDatePojos);

    java.util.Date date = Date.from(endDateTime.toInstant(ZoneOffset.UTC));
    final List<TimeOffAdjustmentPojo> timeOffAdjustmentPojos = timeOffAdjustmentRepository
        .findAllByUserIdAndTimeOffPolicyId(user.getId(), timeOffPolicy.getId(), date);
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
    final DateTimeFormatter timeFormatter =
        DateTimeFormatter.ofPattern(DateUtil.FULL_MONTH_DAY_YEAR);
    final DateTimeFormatter currentYearFormatter =
        DateTimeFormatter.ofPattern(DateUtil.FULL_MONTH_DAY);

    final String startDateString = startDate.getYear() == LocalDate.now().getYear()
        ? startDate.format(currentYearFormatter) : startDate.format(timeFormatter);
    final String endDateString = endDate.getYear() == LocalDate.now().getYear()
        ? endDate.format(currentYearFormatter) : endDate.format(timeFormatter);

    String dateMessage = (startDateString + " - " + endDateString);
    if (startDateString.equals(endDateString)) {
      dateMessage = (startDateString);
    }
    timeOffBreakdownItemDto.setDateMessage(dateMessage);
    timeOffBreakdownItemDto.setDetail("Time Off Taken");
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
          .date(DateUtil.fromTimestamp(timeOffRequestDatePojo.getCreateDate()))
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
