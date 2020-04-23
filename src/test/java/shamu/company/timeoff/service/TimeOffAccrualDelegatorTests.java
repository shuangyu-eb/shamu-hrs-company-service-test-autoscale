package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.TimeOffAccrualFrequencyRepository;

class TimeOffAccrualDelegatorTests {

  private static TimeOffAccrualDelegator timeOffAccrualDelegator;

  @Mock private List<TimeOffAccrualService> accrualServices;

  @Mock private TimeOffAccrualFrequencyRepository timeOffAccrualFrequencyRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    timeOffAccrualDelegator =
        new TimeOffAccrualDelegator(accrualServices, timeOffAccrualFrequencyRepository);
  }

  @Test
  void testGetTimeOffBreakdown() {
    final TimeOffAccrualFrequency timeOffFrequency = new TimeOffAccrualFrequency();
    final TimeOffBreakdownCalculatePojo calculatePojo = new TimeOffBreakdownCalculatePojo();
    final List<TimeOffPolicyAccrualSchedule> trimmedScheduleList = new ArrayList<>();
    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();

    timeOffPolicyUser.setInitialBalance(100);
    timeOffPolicyUser.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
    calculatePojo.setPolicyUser(timeOffPolicyUser);
    calculatePojo.setTrimmedScheduleList(trimmedScheduleList);

    Mockito.when(timeOffAccrualFrequencyRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(timeOffFrequency));

    Assertions.assertThrows(
        ForbiddenException.class,
        () -> timeOffAccrualDelegator.getTimeOffBreakdown("1", calculatePojo));
  }

  @Test
  void whenTimeOffFrequencyIsEmpty_thenShouldThrows() {

    Mockito.when(timeOffAccrualFrequencyRepository.findById(Mockito.any()))
        .thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () ->
            timeOffAccrualDelegator.getTimeOffBreakdown("1", new TimeOffBreakdownCalculatePojo()));
  }
}
