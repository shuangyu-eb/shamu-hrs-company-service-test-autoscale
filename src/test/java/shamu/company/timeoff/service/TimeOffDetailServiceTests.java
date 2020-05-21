package shamu.company.timeoff.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import shamu.company.common.exception.ResourceNotFoundException;
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
          timeOffDetailService.getTimeOffRequestDatesAbstract(timeOffRequest.getId());
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
          timeOffDetailService.getTimeOffRequestDatesAbstract(timeOffRequest.getId());
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
          timeOffDetailService.getTimeOffRequestDatesAbstract(timeOffRequest.getId());
      final String expectedTimeOffRange =
          "Dec 2 - 4, 20, 27, " + currentYear + ", Aug 4 - 6, 19, 21, " + nextYear;
      Assertions.assertEquals(expectedTimeOffRange, result);
    }
  }
}
