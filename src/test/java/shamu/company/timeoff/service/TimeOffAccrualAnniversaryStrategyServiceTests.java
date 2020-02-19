package shamu.company.timeoff.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.timeoff.dto.TimeOffBreakdownAnniversaryDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class TimeOffAccrualAnniversaryStrategyServiceTests {

  @InjectMocks
  private static TimeOffAccrualAnniversaryStrategyService timeOffAccrualAnniversaryStrategyService;

  @Mock
  private AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void getTimeOffBreakdownInternal() {
    final TimeOffBreakdownYearDto timeOffBreakdownYearDto = new TimeOffBreakdownYearDto();
    final TimeOffBreakdownCalculatePojo timeOffBreakdownCalculatePojo = new TimeOffBreakdownCalculatePojo();
    final List<TimeOffPolicyAccrualSchedule> timeOffPolicyAccrualSchedules = new ArrayList<>();
    final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule = new TimeOffPolicyAccrualSchedule();
    final List<AccrualScheduleMilestone> accrualScheduleMilestoneList = new ArrayList<>();
    final AccrualScheduleMilestone accrualScheduleMilestone = new AccrualScheduleMilestone();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency();
    final List<TimeOffBreakdownItemDto> timeOffBreakdownItemDtos = new ArrayList<>();
    final TimeOffBreakdownItemDto timeOffBreakdownItemDto = new TimeOffBreakdownItemDto();

    timeOffAccrualFrequency.setName("007");

    timeOffPolicyUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

    timeOffPolicyAccrualSchedule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    timeOffPolicyAccrualSchedule.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));
    timeOffPolicyAccrualSchedules.add(timeOffPolicyAccrualSchedule);
    timeOffPolicyAccrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);

    timeOffBreakdownCalculatePojo.setTrimmedScheduleList(timeOffPolicyAccrualSchedules);
    timeOffBreakdownCalculatePojo.setPolicyUser(timeOffPolicyUser);

    timeOffBreakdownItemDto.setDate(LocalDate.now());
    timeOffBreakdownItemDto.setAmount(10);
    timeOffBreakdownItemDtos.add(timeOffBreakdownItemDto);
    timeOffBreakdownCalculatePojo.setBalanceAdjustment(timeOffBreakdownItemDtos);

    accrualScheduleMilestone.setAnniversaryYear(1);
    accrualScheduleMilestone.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));
    accrualScheduleMilestone.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    accrualScheduleMilestoneList.add(accrualScheduleMilestone);

    timeOffBreakdownYearDto.setDate(LocalDate.now());
    timeOffBreakdownYearDto.setAccrualHours(10);

    Mockito.when(accrualScheduleMilestoneRepository.findByAccrualScheduleIdWithExpired(Mockito.any())).thenReturn(accrualScheduleMilestoneList);

    Assertions.assertDoesNotThrow(() ->
        timeOffAccrualAnniversaryStrategyService.getTimeOffBreakdownInternal(timeOffBreakdownYearDto, timeOffBreakdownCalculatePojo));
  }

  @Nested
  class addToResultAnniversaryMap {

    @Test
    void whenCurrentYearMapIsNull_thenShouldSuccess() {
      final HashMap<Integer, HashMap<Integer, TimeOffBreakdownAnniversaryDto>> accrualData = new HashMap<>();
      final List<LocalDate> validPeriods = new ArrayList<>();

      validPeriods.add(LocalDate.now());
      validPeriods.add(LocalDate.now());

      Assertions.assertDoesNotThrow(() ->
          Whitebox.invokeMethod(timeOffAccrualAnniversaryStrategyService,"addToResultAnniversaryMap", accrualData, validPeriods, new TimeOffBreakdownAnniversaryDto(), LocalDate.now()));
    }
  }
}
