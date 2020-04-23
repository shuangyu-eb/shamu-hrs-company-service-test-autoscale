package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownMonthDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.user.entity.User;

public class TimeOffAccrualMonthStrategyServiceTests {

  @Mock private AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  private TimeOffAccrualMonthStrategyService timeOffAccrualMonthStrategyService;

  private TimeOffBreakdownDto timeOffBreakdownDto;
  private TimeOffBreakdownYearDto timeOffBreakdownYearDto;
  private TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo;
  private TimeOffBreakdownMonthDto timeOffBreakdownMonthDto;
  private User user;
  private TimeOffPolicyUser timeOffPolicyUser;
  private List<TimeOffPolicyAccrualSchedule> scheduleList;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    user = new User();
    scheduleList = new ArrayList<>();
    timeOffPolicyUser = new TimeOffPolicyUser();
    timeOffBreakdownDto = new TimeOffBreakdownDto();
    timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    timeOffBreakdownCalculatePojo = new TimeOffBreakdownCalculatePojo();
    timeOffBreakdownMonthDto = new TimeOffBreakdownMonthDto();
    timeOffAccrualMonthStrategyService =
        new TimeOffAccrualMonthStrategyService(accrualScheduleMilestoneRepository);
  }

  @Test
  void testGetTimeOffBreakdownInternal() {
    timeOffBreakdownMonthDto.setYearData(timeOffBreakdownYearDto);
    final TimeOffAccrualMonthStrategyService spy = Mockito.spy(timeOffAccrualMonthStrategyService);
    Mockito.doReturn(timeOffBreakdownDto)
        .when(spy)
        .getTimeOffBreakdown(timeOffBreakdownMonthDto, timeOffBreakdownCalculatePojo);
    Assertions.assertNotNull(
        spy.getTimeOffBreakdownInternal(timeOffBreakdownYearDto, timeOffBreakdownCalculatePojo));
  }

  @Test
  void testGetTimeOffBreakdown() {
    final LocalDate today = LocalDate.now();
    final Timestamp timestamp = new Timestamp(15);
    timeOffBreakdownMonthDto.setYearData(timeOffBreakdownYearDto);
    user.setCreatedAt(timestamp);
    timeOffPolicyUser.setUser(user);
    timeOffPolicyUser.setCreatedAt(timestamp);
    timeOffBreakdownCalculatePojo.setPolicyUser(timeOffPolicyUser);

    final List<TimeOffBreakdownItemDto> balanceAdjustment = new ArrayList<>();
    final TimeOffBreakdownItemDto itemDto = new TimeOffBreakdownItemDto();
    itemDto.setDate(LocalDate.of(1970, 1, 1));
    itemDto.setAmount(1);
    balanceAdjustment.add(itemDto);

    final List<AccrualScheduleMilestone> accrualScheduleMilestoneList = new ArrayList<>();
    final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        new TimeOffPolicyAccrualSchedule();
    final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency();
    final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();

    timeOffAccrualFrequency.setId("1");
    timeOffAccrualFrequency.setName("name");

    timeOffPolicyAccrualSchedule.setExpiredAt(timestamp);
    timeOffPolicyAccrualSchedule.setCreatedAt(timestamp);
    timeOffPolicyAccrualSchedule.setAccrualHours(8);
    timeOffPolicyAccrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);
    timeOffPolicyAccrualSchedule.setId("1");

    scheduleList.add(timeOffPolicyAccrualSchedule);
    timeOffBreakdownCalculatePojo.setTrimmedScheduleList(scheduleList);

    accrualScheduleMilestone.setCreatedAt(timestamp);
    accrualScheduleMilestone.setExpiredAt(new Timestamp(200));
    accrualScheduleMilestone.setAnniversaryYear(1);
    accrualScheduleMilestoneList.add(accrualScheduleMilestone);

    Mockito.when(
            accrualScheduleMilestoneRepository.findByAccrualScheduleIdWithExpired(
                Mockito.anyString()))
        .thenReturn(accrualScheduleMilestoneList);
    timeOffBreakdownMonthDto.setDate(today);
    timeOffBreakdownMonthDto.setAccrualHours(1);
    timeOffBreakdownCalculatePojo.setBalanceAdjustment(balanceAdjustment);
    Assertions.assertDoesNotThrow(
        () ->
            timeOffAccrualMonthStrategyService.getTimeOffBreakdown(
                timeOffBreakdownMonthDto, timeOffBreakdownCalculatePojo));
  }
}
