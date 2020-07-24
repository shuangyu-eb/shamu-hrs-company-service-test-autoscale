package shamu.company.timeoff.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.timeoff.dto.TimeOffBreakdownAnniversaryDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownMonthDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.pojo.TimeOffRequestDatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffAdjustmentRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;
import shamu.company.user.entity.User;
import shamu.company.utils.UuidUtil;

class TimeOffDetailServiceTests {
  private static TimeOffDetailService timeOffDetailService;

  @Mock private TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Mock private TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  @Mock private TimeOffRequestDateRepository timeOffRequestDateRepository;

  @Mock private TimeOffAdjustmentRepository timeOffAdjustmentRepository;

  @Mock private AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Mock private TimeOffAccrualDelegator timeOffAccrualDelegator;

  @Mock private TimeOffAccrualNatureStrategyService accrualNatureStrategyService;

  @Mock private TimeOffAccrualAnniversaryStrategyService accrualAnniversaryStrategyService;

  @Mock private TimeOffAccrualMonthStrategyService accrualMonthStrategyService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    timeOffDetailService =
        new TimeOffDetailService(
            timeOffPolicyUserRepository,
            timeOffPolicyAccrualScheduleRepository,
            timeOffRequestDateRepository,
            timeOffAdjustmentRepository,
            accrualScheduleMilestoneRepository,
            timeOffAccrualDelegator);
  }

  @Nested
  class getTimeOffBreakdown {
    TimeOffPolicyUser timeOffPolicyUserContainer;

    @BeforeEach
    void setUp() {
      timeOffPolicyUserContainer = new TimeOffPolicyUser();
    }

    @Test
    void whenUntilDateIsNullAndTimeOffPolicyUserContainerIsNull_thenShouldSuccess() {
      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any()))
          .thenReturn(Optional.empty());

      Assertions.assertDoesNotThrow(
          () -> timeOffDetailService.getTimeOffBreakdown("1", Mockito.any()));
      Assertions.assertNull(timeOffDetailService.getTimeOffBreakdown("1", Mockito.any()));
    }

    @Test
    void
        whenUntilDateIsNotNullAndTimeOffPolicyUserContainerIsNotNullAndLimited_thenShouldSuccess() {
      final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setIsLimited(true);
      timeOffPolicyUserContainer.setTimeOffPolicy(timeOffPolicy);

      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(timeOffPolicyUserContainer));

      Assertions.assertDoesNotThrow(() -> timeOffDetailService.getTimeOffBreakdown("1", 1L));
    }

    @Test
    void
        whenUntilDateIsNotNullAndTimeOffPolicyUserContainerIsNotNullAndUnLimited_thenShouldSuccess() {
      final TimeOffPolicy timeOffPolicy = new TimeOffPolicy("1");
      timeOffPolicy.setIsLimited(false);
      timeOffPolicyUserContainer.setTimeOffPolicy(timeOffPolicy);
      timeOffPolicyUserContainer.setUser(new User("1"));

      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(timeOffPolicyUserContainer));

      Assertions.assertDoesNotThrow(() -> timeOffDetailService.getTimeOffBreakdown("1", 1L));
    }
  }

  @Nested
  class checkTimeOffAdjustments {
    TimeOffPolicyUser timeOffPolicyUser;

    @BeforeEach
    void init() {
      timeOffPolicyUser = new TimeOffPolicyUser();
    }

    @Test
    void whenTimeOffPolicyUserIsEmpty_thenShouldThrows() {
      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any()))
          .thenReturn(Optional.empty());

      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> timeOffDetailService.checkTimeOffAdjustments("1", 100));
    }

    @Test
    void whenTimeOffPolicyUserIsNotEmpty_thenShouldThrows() {

      final User user = new User("1");
      user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
      timeOffPolicyUser.setUser(user);
      timeOffPolicyUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

      final TimeOffPolicyAccrualSchedule accrualSchedule = new TimeOffPolicyAccrualSchedule();
      final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency("1");
      accrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);

      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(timeOffPolicyUser));
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any()))
          .thenReturn(accrualSchedule);

      Assertions.assertDoesNotThrow(() -> timeOffDetailService.checkTimeOffAdjustments("1", 100));
    }
  }

  @Nested
  class getLimitedTimeOffBreakdown {
    List<TimeOffPolicyAccrualSchedule> timeOffPolicyScheduleList;

    @BeforeEach
    void init() {
      timeOffPolicyScheduleList = new ArrayList<>();
    }

    @Test
    void whenTimeOffPolicyScheduleListIsEmpty_thenShouldReturnNull() throws Exception {
      Mockito.when(
              timeOffPolicyAccrualScheduleRepository.findAllWithExpiredTimeOffPolicy(Mockito.any()))
          .thenReturn(timeOffPolicyScheduleList);

      Assertions.assertDoesNotThrow(
          () -> {
            Whitebox.invokeMethod(
                timeOffDetailService,
                "getLimitedTimeOffBreakdown",
                new TimeOffPolicyUser(),
                LocalDate.now());
          });
      Assertions.assertNull(
          Whitebox.invokeMethod(
              timeOffDetailService,
              "getLimitedTimeOffBreakdown",
              new TimeOffPolicyUser(),
              LocalDate.now()));
    }

    @Test
    void whenTimeOffPolicyScheduleListIsNotEmpty_thenShouldSuccess() {
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          new TimeOffPolicyAccrualSchedule();
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule2 =
          new TimeOffPolicyAccrualSchedule();
      final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency("1");
      final List<TimeOffRequestDatePojo> timeOffRequestDatePojos = new ArrayList<>();
      final List<Timestamp> dates = new ArrayList<>();
      final TimeOffRequestDatePojo timeOffRequestDatePojo =
          new TimeOffRequestDatePojo() {
            @Override
            public String getId() {
              return UuidUtil.getUuidString();
            }

            @Override
            public Timestamp getStartDate() {
              return Timestamp.valueOf(LocalDateTime.now());
            }

            @Override
            public Integer getHours() {
              return 10;
            }
          };
      timeOffRequestDatePojos.add(timeOffRequestDatePojo);

      timeOffPolicyAccrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);
      timeOffPolicyAccrualSchedule.setId("1");
      timeOffPolicyAccrualSchedule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

      timeOffPolicyAccrualSchedule2.setTimeOffAccrualFrequency(timeOffAccrualFrequency);
      timeOffPolicyAccrualSchedule2.setId("2");
      timeOffPolicyAccrualSchedule2.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));
      timeOffPolicyAccrualSchedule2.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
      timeOffPolicyAccrualSchedule2.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

      timeOffPolicyScheduleList.add(timeOffPolicyAccrualSchedule);
      timeOffPolicyScheduleList.add(timeOffPolicyAccrualSchedule2);

      final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
      final User user = new User("1");
      timeOffPolicyUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
      timeOffPolicyUser.setUser(user);
      timeOffPolicyUser.setTimeOffPolicy(new TimeOffPolicy("1"));

      // Dec 30, 2018 - Jan 2, 2019
      dates.add(new Timestamp(1546099200000L));
      dates.add(new Timestamp(1546185600000L));
      dates.add(new Timestamp(1546272000000L));
      dates.add(new Timestamp(1546358400000L));

      Mockito.when(
              timeOffPolicyAccrualScheduleRepository.findAllWithExpiredTimeOffPolicy(Mockito.any()))
          .thenReturn(timeOffPolicyScheduleList);
      Mockito.when(
              timeOffRequestDateRepository.getTakenApprovedRequestOffByUserIdAndPolicyId(
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString()))
          .thenReturn(timeOffRequestDatePojos);
      Mockito.when(
              timeOffRequestDateRepository.getTimeOffRequestDatesByTimeOffRequestId(Mockito.any()))
          .thenReturn(dates);

      Assertions.assertDoesNotThrow(
          () -> {
            Whitebox.invokeMethod(
                timeOffDetailService,
                "getLimitedTimeOffBreakdown",
                timeOffPolicyUser,
                LocalDate.now());
          });
    }

    @Test
    void testTimeOffRangeOver3Years() {
      final TimeOffRequest timeOffRequest = new TimeOffRequest();
      final List<Timestamp> dates = new ArrayList<>();
      final int currentYear = LocalDateTime.now().getYear();
      final int lastYear = currentYear - 1;
      final int nextYear = currentYear + 1;
      dates.add(Timestamp.valueOf(lastYear + "-10-10 08:00:00"));
      dates.add(Timestamp.valueOf(lastYear + "-11-30 08:00:00"));
      dates.add(Timestamp.valueOf(lastYear + "-12-01 08:00:00"));
      dates.add(Timestamp.valueOf(lastYear + "-12-31 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-01-01 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-03-01 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-04-30 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-05-01 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-08-08 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-08-22 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-09-13 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-12-31 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-01-01 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-01-21 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-01-31 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-02-01 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-02-06 08:00:00"));

      Mockito.when(
              timeOffRequestDateRepository.getTimeOffRequestDatesByTimeOffRequestId(Mockito.any()))
          .thenReturn(dates);
      final String result =
          timeOffDetailService.getTimeOffRequestDatesPreview(timeOffRequest.getId());
      final String expectedTimeOffRange =
          "Oct 10, Nov 30 - Dec 1, 31, "
              + lastYear
              + " - Jan 1,"
              + " Mar 1, Apr 30 - May 1, Aug 8, 22, Sep 13, "
              + "Dec 31, "
              + currentYear
              + " - Jan 1, 21, 31 - Feb 1, 6, "
              + nextYear;
      Assertions.assertEquals(expectedTimeOffRange, result);
    }

    @Test
    void testTimeOffRangeOver2Years() {
      final TimeOffRequest timeOffRequest = new TimeOffRequest();
      final List<Timestamp> dates = new ArrayList<>();
      final int currentYear = LocalDateTime.now().getYear();
      final int nextYear = currentYear + 1;
      dates.add(Timestamp.valueOf(currentYear + "-11-15 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-11-22 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-07-08 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-07-10 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-08-07 08:00:00"));

      Mockito.when(
              timeOffRequestDateRepository.getTimeOffRequestDatesByTimeOffRequestId(Mockito.any()))
          .thenReturn(dates);
      final String result =
          timeOffDetailService.getTimeOffRequestDatesPreview(timeOffRequest.getId());
      final String expectedTimeOffRange =
          "Nov 15, 22, " + currentYear + ", Jul 8, 10, Aug 7, " + nextYear;
      Assertions.assertEquals(expectedTimeOffRange, result);
    }

    @Test
    void testTimeOffRangeOver2YearsAndSingleDate() {
      final TimeOffRequest timeOffRequest = new TimeOffRequest();
      final List<Timestamp> dates = new ArrayList<>();
      final int currentYear = LocalDateTime.now().getYear();
      final int nextYear = currentYear + 1;
      dates.add(Timestamp.valueOf(currentYear + "-12-02 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-12-03 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-12-04 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-12-20 08:00:00"));
      dates.add(Timestamp.valueOf(currentYear + "-12-27 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-08-04 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-08-05 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-08-06 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-08-19 08:00:00"));
      dates.add(Timestamp.valueOf(nextYear + "-08-21 08:00:00"));

      Mockito.when(
              timeOffRequestDateRepository.getTimeOffRequestDatesByTimeOffRequestId(Mockito.any()))
          .thenReturn(dates);
      final String result =
          timeOffDetailService.getTimeOffRequestDatesPreview(timeOffRequest.getId());
      final String expectedTimeOffRange =
          "Dec 2 - 4, 20, 27, " + currentYear + ", Aug 4 - 6, 19, 21, " + nextYear;
      Assertions.assertEquals(expectedTimeOffRange, result);
    }
  }

  @Test
  void whenDifferentYear_thenReturnMoreElements() throws Exception {

    final LocalDate startDate = LocalDate.of(2016, 1, 1);
    final LocalDate endDate = LocalDate.of(2017, 2, 2);

    final List<Integer> validYears =
        Whitebox.invokeMethod(
            accrualNatureStrategyService,
            "getValidYearPeriod",
            startDate,
            endDate,
            LocalDate.now());

    Assertions.assertEquals(2, validYears.size());
  }

  @Nested
  class GetValidYearPeriod {

    @Test
    void whenSameYear_thenReturnOneElement() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 1, 1);
      final LocalDate endDate = LocalDate.of(2016, 2, 2);

      final List<Integer> validYears =
          Whitebox.invokeMethod(
              accrualNatureStrategyService,
              "getValidYearPeriod",
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertEquals(1, validYears.size());
    }
  }

  @Nested
  class GetValidAnniversaryPeriod {

    @Test
    void whenSameAnniversary_thenReturnEmpty() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 6, 1);
      final LocalDate endDate = LocalDate.of(2017, 3, 2);

      final LocalDate userJoinDate = LocalDate.of(2016, 4, 4);

      final List<LocalDate> validYears =
          Whitebox.invokeMethod(
              accrualAnniversaryStrategyService,
              "getValidAnniversaryPeriod",
              userJoinDate,
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertTrue(validYears.isEmpty());
    }

    @Test
    void whenDifferentAnniversary_thenReturnFilledArray() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 2, 1);
      final LocalDate endDate = LocalDate.of(2017, 4, 1);

      final LocalDate userJoinDate = LocalDate.of(2016, 3, 4);

      final List<LocalDate> validYears =
          Whitebox.invokeMethod(
              accrualAnniversaryStrategyService,
              "getValidAnniversaryPeriod",
              userJoinDate,
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertAll(
          () -> Assertions.assertEquals(1, validYears.size()),
          () -> Assertions.assertTrue(validYears.get(0).isEqual(userJoinDate)));
    }
  }

  @Nested
  class GetValidMonthPeriod {

    @Test
    void whenAfterEndDate_thenReturnEmpty() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 6, 1);
      final LocalDate endDate = LocalDate.of(2016, 5, 2);

      final List<LocalDate> monthPeriods =
          Whitebox.invokeMethod(
              accrualMonthStrategyService,
              "getValidMonthPeriod",
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertTrue(monthPeriods.isEmpty());
    }

    @Test
    void whenBeforeEndDate_thenReturnFilledArray() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 6, 2);
      final LocalDate endDate = LocalDate.of(2016, 7, 1);

      final List<LocalDate> monthPeriods =
          Whitebox.invokeMethod(
              accrualMonthStrategyService,
              "getValidMonthPeriod",
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertEquals(1, monthPeriods.size());
    }
  }

  @Nested
  class AddMissingYearDto {

    @Test
    void whenMissingOneYear_thenReturnFilled() throws Exception {
      final List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtos = new ArrayList<>();
      final TimeOffBreakdownYearDto startBreakdownYearDto = new TimeOffBreakdownYearDto();
      startBreakdownYearDto.setDate(LocalDate.MIN.withYear(2016));
      timeOffBreakdownYearDtos.add(startBreakdownYearDto);

      final TimeOffBreakdownYearDto endBreakdownYearDto = new TimeOffBreakdownYearDto();
      endBreakdownYearDto.setDate(LocalDate.MIN.withYear(2018));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      final LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(
              accrualNatureStrategyService, "addMissingYearDto", timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class AddMissingAnniversaryYearDto {

    @Test
    void whenMissingOneAnniversaryYear_thenReturnFilled() throws Exception {
      final List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownYearDtos = new ArrayList<>();
      final TimeOffBreakdownAnniversaryDto startBreakdownDto = new TimeOffBreakdownAnniversaryDto();
      startBreakdownDto.setDate(LocalDate.of(2016, 1, 1));
      timeOffBreakdownYearDtos.add(startBreakdownDto);

      final TimeOffBreakdownAnniversaryDto endBreakdownYearDto =
          new TimeOffBreakdownAnniversaryDto();
      endBreakdownYearDto.setDate(LocalDate.of(2018, 1, 1));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      final LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(
              accrualAnniversaryStrategyService,
              "addMissingAnniversaryYearDto",
              timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class AddMissingMonthDto {

    @Test
    void whenMissingOneMonth_thenReturnFilled() throws Exception {
      final List<TimeOffBreakdownMonthDto> timeOffBreakdownYearDtos = new ArrayList<>();
      final TimeOffBreakdownMonthDto startBreakdownDto = new TimeOffBreakdownMonthDto();
      startBreakdownDto.setDate(LocalDate.of(2016, 1, 1));
      timeOffBreakdownYearDtos.add(startBreakdownDto);

      final TimeOffBreakdownMonthDto endBreakdownYearDto = new TimeOffBreakdownMonthDto();
      endBreakdownYearDto.setDate(LocalDate.of(2016, 3, 1));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      final LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(
              accrualMonthStrategyService, "addMissingMonthDto", timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class GetFinalTimeOffBreakdown {

    TimeOffBreakdownYearDto startingBreakdown;

    List<TimeOffBreakdownItemDto> balanceAdjustmentList = new ArrayList<>();

    @BeforeEach
    void setUp() {
      startingBreakdown = new TimeOffBreakdownYearDto();
      startingBreakdown.setDate(LocalDate.of(2016, 3, 10));
      startingBreakdown.setAccrualHours(10);

      final TimeOffBreakdownItemDto timeOffBreakdownItemDto = new TimeOffBreakdownItemDto();
      timeOffBreakdownItemDto.setDate(LocalDate.of(2016, 5, 5));
      timeOffBreakdownItemDto.setAmount(-4);
      timeOffBreakdownItemDto.setDetail("Request time off");
      balanceAdjustmentList.add(timeOffBreakdownItemDto);
    }

    @Nested
    class getFinalByYear {

      List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList;

      @BeforeEach
      void setUp() {
        timeOffBreakdownYearDtoList = new ArrayList<>();
        timeOffBreakdownYearDtoList.add(startingBreakdown);
        final TimeOffBreakdownYearDto firstBreakdownYearDto = new TimeOffBreakdownYearDto();
        firstBreakdownYearDto.setAccrualHours(10);
        firstBreakdownYearDto.setDate(LocalDate.MIN.withYear(2016));
        firstBreakdownYearDto.setCarryoverLimit(3);
        firstBreakdownYearDto.setMaxBalance(20);
        timeOffBreakdownYearDtoList.add(firstBreakdownYearDto);

        final TimeOffBreakdownYearDto secondBreakdownYearDto = new TimeOffBreakdownYearDto();
        secondBreakdownYearDto.setAccrualHours(6);
        secondBreakdownYearDto.setDate(LocalDate.MIN.withYear(2017));
        secondBreakdownYearDto.setCarryoverLimit(3);
        secondBreakdownYearDto.setMaxBalance(20);
        timeOffBreakdownYearDtoList.add(secondBreakdownYearDto);
      }

      @Test
      void testGetFinalTimeOffBreakdown() throws Exception {
        final TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(
                accrualNatureStrategyService,
                "getFinalTimeOffBreakdown",
                timeOffBreakdownYearDtoList,
                balanceAdjustmentList);
        Assertions.assertEquals(9, timeOffBreakdownDto.getBalance().intValue());
      }
    }

    @Nested
    class GetFinalByAnniversaryYear {

      List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList = new ArrayList<>();

      @BeforeEach
      void setUp() {
        final TimeOffBreakdownAnniversaryDto firstBreakdownYearDto =
            new TimeOffBreakdownAnniversaryDto();
        final LocalDate firstYearDate = LocalDate.of(2016, 5, 2);
        firstBreakdownYearDto.setDate(firstYearDate);
        firstBreakdownYearDto.setAccrualHours(10);
        firstBreakdownYearDto.setMaxBalance(7);
        firstBreakdownYearDto.setCarryoverLimit(2);
        timeOffBreakdownAnniversaryDtoList.add(firstBreakdownYearDto);

        final TimeOffBreakdownAnniversaryDto secondBreakdownYearDto =
            new TimeOffBreakdownAnniversaryDto();
        final LocalDate secondYearDate = LocalDate.of(2017, 5, 2);
        secondBreakdownYearDto.setAccrualHours(10);
        secondBreakdownYearDto.setDate(secondYearDate);
        secondBreakdownYearDto.setCarryoverLimit(5);
        secondBreakdownYearDto.setMaxBalance(16);
        timeOffBreakdownAnniversaryDtoList.add(secondBreakdownYearDto);
      }

      @Test
      void testGetFinalAnniversaryBreakdown() throws Exception {
        final TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(
                accrualAnniversaryStrategyService,
                "getFinalAnniversaryBreakdown",
                timeOffBreakdownAnniversaryDtoList,
                balanceAdjustmentList);
        Assertions.assertEquals(12, timeOffBreakdownDto.getBalance().intValue());
      }
    }

    @Nested
    class GetFinalMonthBreakdown {

      List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtos = new ArrayList<>();

      @BeforeEach
      void setUp() {
        final TimeOffBreakdownMonthDto firstBreakdownMonthDto = new TimeOffBreakdownMonthDto();
        final LocalDate firstMonthDate = LocalDate.of(2016, 5, 1);
        firstBreakdownMonthDto.setDate(firstMonthDate);
        firstBreakdownMonthDto.setAccrualHours(10);
        firstBreakdownMonthDto.setLastMonthOfPreviousAnniversaryYear(false);
        timeOffBreakdownMonthDtos.add(firstBreakdownMonthDto);

        final TimeOffBreakdownMonthDto secondBreakdownMonthDto = new TimeOffBreakdownMonthDto();
        final LocalDate secondMonthDate = LocalDate.of(2016, 6, 1);
        secondBreakdownMonthDto.setAccrualHours(10);
        secondBreakdownMonthDto.setDate(secondMonthDate);
        secondBreakdownMonthDto.setLastMonthOfPreviousAnniversaryYear(false);
        timeOffBreakdownMonthDtos.add(secondBreakdownMonthDto);
      }

      @Test
      void testGetFinalMonthBreakdown() throws Exception {
        final TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(
                accrualMonthStrategyService,
                "getFinalMonthBreakdown",
                timeOffBreakdownMonthDtos,
                balanceAdjustmentList);
        Assertions.assertEquals(16, timeOffBreakdownDto.getBalance().intValue());
      }
    }
  }
}
