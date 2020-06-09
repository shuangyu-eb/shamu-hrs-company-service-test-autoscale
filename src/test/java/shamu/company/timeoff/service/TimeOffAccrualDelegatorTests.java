package shamu.company.timeoff.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.exception.NotFoundException;
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
    timeOffPolicyUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    calculatePojo.setPolicyUser(timeOffPolicyUser);
    calculatePojo.setTrimmedScheduleList(trimmedScheduleList);

    Mockito.when(timeOffAccrualFrequencyRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(timeOffFrequency));

    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> timeOffAccrualDelegator.getTimeOffBreakdown("1", calculatePojo));
  }

  @Test
  void whenTimeOffFrequencyIsEmpty_thenShouldThrows() {

    Mockito.when(timeOffAccrualFrequencyRepository.findById(Mockito.any()))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(
            () ->
                timeOffAccrualDelegator.getTimeOffBreakdown(
                    "1", new TimeOffBreakdownCalculatePojo()));
  }
}
