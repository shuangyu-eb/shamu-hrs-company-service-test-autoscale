package shamu.company.timeoff.service;

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
import shamu.company.timeoff.pojo.TimeOffRequestDatePojo;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffAdjustmentRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;
import shamu.company.user.entity.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


class TimeOffDetailServiceTests {
  private static TimeOffDetailService timeOffDetailService;

  @Mock
  private TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Mock
  private TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  @Mock
  private TimeOffRequestDateRepository timeOffRequestDateRepository;

  @Mock
  private TimeOffAdjustmentRepository timeOffAdjustmentRepository;

  @Mock
  private AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  @Mock
  private TimeOffAccrualDelegator timeOffAccrualDelegator;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    timeOffDetailService = new TimeOffDetailService(
        timeOffPolicyUserRepository,
        timeOffPolicyAccrualScheduleRepository,
        timeOffRequestDateRepository,
        timeOffAdjustmentRepository,
        accrualScheduleMilestoneRepository,
        timeOffAccrualDelegator
    );
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
      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());

      Assertions.assertDoesNotThrow(() ->
          timeOffDetailService.getTimeOffBreakdown("1", Mockito.any()));
      Assertions.assertNull(timeOffDetailService.getTimeOffBreakdown("1", Mockito.any()));
    }

    @Test
    void whenUntilDateIsNotNullAndTimeOffPolicyUserContainerIsNotNullAndLimited_thenShouldSuccess() {
      final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setIsLimited(true);
      timeOffPolicyUserContainer.setTimeOffPolicy(timeOffPolicy);

      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any())).thenReturn(Optional.of(timeOffPolicyUserContainer));

      Assertions.assertDoesNotThrow(() ->
          timeOffDetailService.getTimeOffBreakdown("1", 1l));
    }

    @Test
    void whenUntilDateIsNotNullAndTimeOffPolicyUserContainerIsNotNullAndUnLimited_thenShouldSuccess() {
      final TimeOffPolicy timeOffPolicy = new TimeOffPolicy("1");
      timeOffPolicy.setIsLimited(false);
      timeOffPolicyUserContainer.setTimeOffPolicy(timeOffPolicy);
      timeOffPolicyUserContainer.setUser(new User("1"));

      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any())).thenReturn(Optional.of(timeOffPolicyUserContainer));

      Assertions.assertDoesNotThrow(() ->
          timeOffDetailService.getTimeOffBreakdown("1", 1l));
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
      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any())).thenReturn(Optional.empty());

      Assertions.assertThrows(ResourceNotFoundException.class, () -> timeOffDetailService.checkTimeOffAdjustments("1", 100));
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

      Mockito.when(timeOffPolicyUserRepository.findById(Mockito.any())).thenReturn(Optional.of(timeOffPolicyUser));
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any())).thenReturn(accrualSchedule);

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
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findAllWithExpiredTimeOffPolicy(Mockito.any())).thenReturn(timeOffPolicyScheduleList);

      Assertions.assertDoesNotThrow(() -> {
        Whitebox.invokeMethod(timeOffDetailService,"getLimitedTimeOffBreakdown",new TimeOffPolicyUser(), LocalDate.now());
      });
      Assertions.assertNull(Whitebox.invokeMethod(timeOffDetailService,"getLimitedTimeOffBreakdown",new TimeOffPolicyUser(), LocalDate.now()));
    }

    @Test
    void whenTimeOffPolicyScheduleListIsNotEmpty_thenShouldSuccess() throws Exception {
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule = new TimeOffPolicyAccrualSchedule();
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule2 = new TimeOffPolicyAccrualSchedule();
      final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency("1");
      final List<TimeOffRequestDatePojo> timeOffRequestDatePojos = new ArrayList<>();
      final TimeOffRequestDatePojo timeOffRequestDatePojo = new TimeOffRequestDatePojo() {
        @Override
        public Timestamp getCreateDate() {
          return Timestamp.valueOf(LocalDateTime.now());
        }

        @Override
        public Timestamp getStartDate() {
          return Timestamp.valueOf(LocalDateTime.now());
        }

        @Override
        public Timestamp getEndDate() {
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
      user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
      timeOffPolicyUser.setUser(user);
      timeOffPolicyUser.setTimeOffPolicy(new TimeOffPolicy("1"));

      Mockito.when(timeOffPolicyAccrualScheduleRepository.findAllWithExpiredTimeOffPolicy(Mockito.any())).thenReturn(timeOffPolicyScheduleList);
      Mockito.when(timeOffRequestDateRepository.getTakenApprovedRequestOffByUserIdAndPolicyId(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(timeOffRequestDatePojos);

      Assertions.assertDoesNotThrow(() -> {
        Whitebox.invokeMethod(timeOffDetailService,"getLimitedTimeOffBreakdown",timeOffPolicyUser, LocalDate.now());
      });
    }
  }
}
