package shamu.company.timeoff.service;

import static java.util.Date.from;
import static shamu.company.timeoff.service.TimeOffAccrualService.invalidByStartDateAndEndDate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.timeoff.dto.TimeOffAdjustmentCheckDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.pojo.TimeOffAdjustmentPojo;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.pojo.TimeOffRequestDatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
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
  private final AccrualScheduleMilestoneRepository milestoneRepository;
  private final TimeOffAccrualDelegator accrualDelegator;

  @Autowired
  public TimeOffDetailService(
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      final TimeOffRequestDateRepository timeOffRequestDateRepository,
      final TimeOffAdjustmentRepository timeOffAdjustmentRepository,
      final AccrualScheduleMilestoneRepository milestoneRepository,
      final TimeOffAccrualDelegator accrualDelegator) {
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
    this.timeOffAdjustmentRepository = timeOffAdjustmentRepository;
    this.milestoneRepository = milestoneRepository;
    this.accrualDelegator = accrualDelegator;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(final String policyUserId, final Long untilDate) {

    LocalDate endDate = LocalDate.now();

    if (untilDate != null) {
      endDate =
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(untilDate), ZoneOffset.UTC).toLocalDate();
    }

    final Optional<TimeOffPolicyUser> timeOffPolicyUserContainer =
        timeOffPolicyUserRepository.findById(policyUserId);

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
      final TimeOffPolicyUser timeOffPolicyUser, final LocalDate endDate) {

    final User user = timeOffPolicyUser.getUser();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();
    final List<TimeOffRequestDatePojo> timeOffRequestDatePojos =
        timeOffRequestDateRepository.getTakenApprovedRequestOffByUserIdAndPolicyId(
            user.getId(),
            timeOffPolicy.getId(),
            DateUtil.getLocalUtcTime(),
            TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED.name());
    final List<TimeOffBreakdownItemDto> requestDateBreakdownList =
        getBreakdownListFromRequestOff(timeOffRequestDatePojos);

    final TimeOffBreakdownDto timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownDto.setShowBalance(false);

    requestDateBreakdownList.sort(Comparator.comparing(TimeOffBreakdownItemDto::getDate));

    final List<TimeOffBreakdownItemDto> breakdownItemDtos =
        requestDateBreakdownList.stream()
            .filter(breakdown -> !breakdown.getDate().isAfter(endDate))
            .collect(Collectors.toList());
    timeOffBreakdownDto.setList(breakdownItemDtos);
    return timeOffBreakdownDto;
  }

  private TimeOffBreakdownDto getLimitedTimeOffBreakdown(
      final TimeOffPolicyUser timeOffPolicyUser, final LocalDate endDate) {
    final List<TimeOffPolicyAccrualSchedule> timeOffPolicyScheduleList =
        timeOffPolicyAccrualScheduleRepository.findAllWithExpiredTimeOffPolicy(
            timeOffPolicyUser.getTimeOffPolicy());

    if (CollectionUtils.isEmpty(timeOffPolicyScheduleList)) {
      return null;
    }

    final TimeOffAccrualFrequency timeOffFrequency =
        timeOffPolicyScheduleList.get(0).getTimeOffAccrualFrequency();
    final String timeOffFrequencyId = timeOffFrequency.getId();

    final List<TimeOffPolicyAccrualSchedule> trimmedScheduleList =
        trimTimeOffPolicyScheduleList(timeOffPolicyScheduleList, timeOffPolicyUser);

    // Sort schedule list
    trimmedScheduleList.sort(Comparator.comparing(TimeOffPolicyAccrualSchedule::getCreatedAt));

    final List<TimeOffBreakdownItemDto> balanceAdjustment =
        getBalanceAdjustmentList(timeOffPolicyUser, endDate);

    final TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo =
        TimeOffBreakdownCalculatePojo.builder()
            .trimmedScheduleList(trimmedScheduleList)
            .balanceAdjustment(balanceAdjustment)
            .policyUser(timeOffPolicyUser)
            .untilDate(endDate)
            .build();

    return accrualDelegator.getTimeOffBreakdown(timeOffFrequencyId, timeOffBreakdownCalculatePojo);
  }

  private List<TimeOffBreakdownItemDto> getBalanceAdjustmentList(
      final TimeOffPolicyUser timeOffPolicyUser, final LocalDate endDate) {
    final User user = timeOffPolicyUser.getUser();
    final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();

    final LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
    final List<TimeOffRequestDatePojo> timeOffRequestDatePojos =
        timeOffRequestDateRepository.getTakenApprovedRequestOffByUserIdAndPolicyId(
            user.getId(),
            timeOffPolicy.getId(),
            endDateTime,
            TimeOffRequestApprovalStatus.TimeOffApprovalStatus.APPROVED.name());

    final List<TimeOffBreakdownItemDto> requestDateBreakdownList =
        getBreakdownListFromRequestOff(timeOffRequestDatePojos);

    final java.util.Date date = from(endDateTime.toInstant(ZoneOffset.UTC));
    final List<TimeOffAdjustmentPojo> timeOffAdjustmentPojos =
        timeOffAdjustmentRepository.findAllByUserIdAndTimeOffPolicyId(
            user.getId(), timeOffPolicy.getId(), date);
    final List<TimeOffBreakdownItemDto> adjustmentBreakdownList =
        getBreakdownListFromAdjustment(timeOffAdjustmentPojos);

    final List<TimeOffBreakdownItemDto> adjustmentList = new ArrayList<>(requestDateBreakdownList);
    adjustmentList.addAll(adjustmentBreakdownList);
    return adjustmentList;
  }

  private void populateBreakdownItem(
      final List<TimeOffBreakdownItemDto> breakdownItemList,
      final TimeOffBreakdownItemDto timeOffBreakdownItemDto,
      final String timeOffRequestId) {
    final String dateMessage = getTimeOffRequestDatesRange(timeOffRequestId);

    timeOffBreakdownItemDto.setDateMessage(dateMessage);
    timeOffBreakdownItemDto.setDetail("Time Off Taken");
    timeOffBreakdownItemDto.setBreakdownType(
        TimeOffBreakdownItemDto.BreakDownType.TIME_OFF_REQUEST);
    breakdownItemList.add(timeOffBreakdownItemDto);
  }

  public String getTimeOffRequestDatesRange(final String timeOffRequestId) {
    final List<Timestamp> dates =
        timeOffRequestDateRepository.getTimeOffRequestDatesByTimeOffRequestId(timeOffRequestId);
    final StringBuilder dateMessage = new StringBuilder();
    LocalDate time = null;
    Integer previousDateMonth = null;
    Integer previousDateYear = null;
    StringBuilder tempDateMessage = null;
    for (final Timestamp date : dates) {
      final LocalDate localDate = DateUtil.fromTimestamp(date);
      String dateFormat;
      if (previousDateMonth != null && previousDateMonth == localDate.getMonthValue()) {
        dateFormat = DateUtil.DAY;
      } else {
        dateFormat = DateUtil.SIMPLE_MONTH_DAY;
      }
      previousDateMonth = localDate.getMonthValue();
      if (null != time) {
        if (localDate.minusDays(1).equals(time)) {
          if (isConsecutiveDatesAndDifferentYear(dates)
              && time.getMonthValue() == localDate.getMonthValue()) {
            dateFormat = DateUtil.SIMPLE_MONTH_DAY;
          }
          tempDateMessage =
              new StringBuilder(" - ".concat(DateUtil.formatDateTo(localDate, dateFormat)));
          time = localDate;
          continue;
        }
        if (tempDateMessage != null) {
          if (previousDateYear != localDate.getYear()
              && previousDateYear != LocalDate.now().getYear()) {
            dateMessage.append(", ").append(previousDateYear);
          }
          dateMessage.append(tempDateMessage);
          tempDateMessage = null;
        }
        dateMessage.append(", ");
      }
      time = localDate;
      previousDateYear = localDate.getYear();
      dateMessage.append(DateUtil.formatDateTo(localDate, dateFormat));
    }
    if (tempDateMessage != null) {
      if (isConsecutiveDatesAndDifferentYear(dates)) {
        dateMessage.append(", ").append(DateUtil.fromTimestamp(dates.get(0)).getYear());
      }
      dateMessage.append(tempDateMessage);
    }
    if (DateUtil.fromTimestamp(dates.get(dates.size() - 1)).getYear()
        != LocalDate.now().getYear()) {
      dateMessage
          .append(", ")
          .append(DateUtil.fromTimestamp(dates.get(dates.size() - 1)).getYear());
    }
    return dateMessage.toString();
  }

  private boolean isConsecutiveDatesAndDifferentYear(final List<Timestamp> dates) {
    boolean isConsecutiveDates = true;
    final int firstDateYear = DateUtil.fromTimestamp(dates.get(0)).getYear();
    boolean isDifferentYear = false;
    for (int i = 1; i < dates.size(); i++) {
      if (!DateUtil.fromTimestamp(dates.get(i))
          .minusDays(1)
          .equals(DateUtil.fromTimestamp(dates.get(i - 1)))) {
        isConsecutiveDates = false;
      }
      if (DateUtil.fromTimestamp(dates.get(i)).getYear() != firstDateYear) {
        isDifferentYear = true;
        break;
      }
    }
    return isConsecutiveDates && isDifferentYear;
  }

  private List<TimeOffBreakdownItemDto> getBreakdownListFromRequestOff(
      final List<TimeOffRequestDatePojo> timeOffRequestDatePojos) {

    final List<TimeOffBreakdownItemDto> breakdownItemList = new ArrayList<>();
    for (final TimeOffRequestDatePojo timeOffRequestDatePojo : timeOffRequestDatePojos) {
      final TimeOffBreakdownItemDto timeOffBreakdownItemDto =
          TimeOffBreakdownItemDto.builder()
              .amount(-timeOffRequestDatePojo.getHours())
              .date(DateUtil.fromTimestamp(timeOffRequestDatePojo.getStartDate()))
              .build();

      populateBreakdownItem(
          breakdownItemList, timeOffBreakdownItemDto, timeOffRequestDatePojo.getId());
    }
    return breakdownItemList;
  }

  private List<TimeOffBreakdownItemDto> getBreakdownListFromAdjustment(
      final List<TimeOffAdjustmentPojo> timeOffAdjustmentPojos) {
    return timeOffAdjustmentPojos.stream()
        .map(TimeOffBreakdownItemDto::fromTimeOffAdjustment)
        .collect(Collectors.toList());
  }

  private List<TimeOffPolicyAccrualSchedule> trimTimeOffPolicyScheduleList(
      final List<TimeOffPolicyAccrualSchedule> timeOffPolicyAccrualScheduleList,
      final TimeOffPolicyUser policyUser) {

    final List<TimeOffPolicyAccrualSchedule> trimmedScheduleList = new ArrayList<>();
    for (final TimeOffPolicyAccrualSchedule accrualSchedule : timeOffPolicyAccrualScheduleList) {
      final LocalDateTime userEnrollDateTime = DateUtil.toLocalDateTime(policyUser.getCreatedAt());
      final String frequencyId = accrualSchedule.getTimeOffAccrualFrequency().getId();

      if (accrualSchedule.getExpiredAt() != null
          && invalidByStartDateAndEndDate(
              accrualSchedule.getCreatedAt(),
              accrualSchedule.getExpiredAt(),
              userEnrollDateTime,
              frequencyId)) {
        continue;
      }

      trimmedScheduleList.add(accrualSchedule);
    }

    return trimmedScheduleList;
  }

  public TimeOffAdjustmentCheckDto checkTimeOffAdjustments(
      final String policyUserId, final Integer newBalance) {

    final Integer maxBalance = getMaxBalance(policyUserId);
    final boolean exceed = maxBalance != null && newBalance > maxBalance;

    return TimeOffAdjustmentCheckDto.builder().maxBalance(maxBalance).exceed(exceed).build();
  }

  // TODO simplify
  private Integer getMaxBalance(final String policyUserId) {
    final TimeOffPolicyUser timeOffPolicyUser =
        timeOffPolicyUserRepository
            .findById(policyUserId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Time off policy user with id " + policyUserId + " not found!"));
    final TimeOffPolicyAccrualSchedule accrualSchedule =
        timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(
            timeOffPolicyUser.getTimeOffPolicy());

    final LocalDate hireDate = DateUtil.fromTimestamp(timeOffPolicyUser.getUser().getCreatedAt());
    final LocalDate userJoinDate = DateUtil.fromTimestamp(timeOffPolicyUser.getCreatedAt());
    final LocalDate startDate =
        TimeOffAccrualService.getScheduleStartBaseTime(hireDate, userJoinDate, accrualSchedule);

    final LocalDate now = DateUtil.getLocalUtcTime().toLocalDate();
    final long maxYears =
        Duration.between(startDate.atStartOfDay(), now.atStartOfDay()).toDays() / 365L;
    List<AccrualScheduleMilestone> milestones =
        milestoneRepository.findByAccrualScheduleIdAndEndYear(
            accrualSchedule.getId(), (int) maxYears);
    milestones =
        TimeOffAccrualService.trimTimeOffPolicyScheduleMilestones(
            milestones, timeOffPolicyUser, accrualSchedule);

    milestones.sort(
        Comparator.comparing(
            AccrualScheduleMilestone::getAnniversaryYear, Comparator.reverseOrder()));
    final Optional<AccrualScheduleMilestone> targetMilestone =
        milestones.stream()
            .filter(
                (accrualScheduleMilestone ->
                    !startDate
                        .plusYears(accrualScheduleMilestone.getAnniversaryYear())
                        .isAfter(now)))
            .findFirst();

    return targetMilestone.isPresent()
        ? targetMilestone.get().getMaxBalance()
        : accrualSchedule.getMaxBalance();
  }
}
