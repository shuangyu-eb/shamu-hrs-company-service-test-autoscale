package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;

class TimeOffAccrualNatureStrategyServiceTests {

  @InjectMocks
  private static TimeOffAccrualNatureStrategyService timeOffAccrualNatureStrategyService;

  @Mock private AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void getTimeOffBreakdownInternal() {
    final TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    final TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo =
        new TimeOffBreakdownCalculatePojo();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffPolicyUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    timeOffBreakdownCalculatePojo.setPolicyUser(timeOffPolicyUser);

    final List<TimeOffPolicyAccrualSchedule> timeOffPolicyAccrualScheduleList = new ArrayList<>();
    final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        new TimeOffPolicyAccrualSchedule();
    timeOffPolicyAccrualSchedule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    timeOffPolicyAccrualSchedule.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));
    timeOffBreakdownCalculatePojo.setTrimmedScheduleList(timeOffPolicyAccrualScheduleList);

    final List<TimeOffBreakdownItemDto> timeOffBreakdownItemDtoList = new ArrayList<>();
    timeOffBreakdownCalculatePojo.setBalanceAdjustment(timeOffBreakdownItemDtoList);

    timeOffBreakdownYearDto.setAccrualHours(10);
    timeOffBreakdownYearDto.setDate(LocalDate.now());

    Assertions.assertDoesNotThrow(
        () ->
            timeOffAccrualNatureStrategyService.getTimeOffBreakdownInternal(
                timeOffBreakdownYearDto, timeOffBreakdownCalculatePojo));
  }

  @Test
  void getAccrualDataByYear() {
    final TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo =
        new TimeOffBreakdownCalculatePojo();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final List<TimeOffPolicyAccrualSchedule> timeOffPolicyAccrualSchedules = new ArrayList<>();
    final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        new TimeOffPolicyAccrualSchedule();
    final List<AccrualScheduleMilestone> accrualScheduleMilestones = new ArrayList<>();
    final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();
    final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency();

    accrualScheduleMilestone.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    accrualScheduleMilestone.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));
    accrualScheduleMilestone.setAnniversaryYear(2020);

    accrualScheduleMilestones.add(accrualScheduleMilestone);

    timeOffPolicyAccrualSchedule.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));
    timeOffPolicyAccrualSchedule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    timeOffPolicyAccrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);

    timeOffPolicyAccrualSchedules.add(timeOffPolicyAccrualSchedule);

    timeOffPolicyUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

    timeOffBreakdownCalculatePojo.setPolicyUser(timeOffPolicyUser);
    timeOffBreakdownCalculatePojo.setTrimmedScheduleList(timeOffPolicyAccrualSchedules);
    timeOffBreakdownCalculatePojo.setUntilDate(LocalDate.now());

    Mockito.when(
            accrualScheduleMilestoneRepository.findByAccrualScheduleIdWithExpired(Mockito.any()))
        .thenReturn(accrualScheduleMilestones);

    Assertions.assertDoesNotThrow(
        () ->
            Whitebox.invokeMethod(
                timeOffAccrualNatureStrategyService,
                "getAccrualDataByYear",
                timeOffBreakdownCalculatePojo));
  }

  @Test
  void getAccrualBreakdownYearMap() {
    final List<Integer> validYears = new ArrayList<>();

    validYears.add(1);
    validYears.add(2);

    Assertions.assertDoesNotThrow(
        () ->
            Whitebox.invokeMethod(
                timeOffAccrualNatureStrategyService,
                "getAccrualBreakdownYearMap",
                validYears,
                new TimeOffBreakdownYearDto(),
                LocalDate.now()));
  }
}
