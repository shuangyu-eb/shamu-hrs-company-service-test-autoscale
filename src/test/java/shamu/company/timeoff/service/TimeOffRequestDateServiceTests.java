package shamu.company.timeoff.service;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;

class TimeOffRequestDateServiceTests {
  private static TimeOffRequestDateService timeOffRequestDateService;

  @Mock private TimeOffRequestDateRepository timeOffRequestDateRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    timeOffRequestDateService = new TimeOffRequestDateService(timeOffRequestDateRepository);
  }

  @Test
  void saveAllTimeOffRequestDates() {
    final List<TimeOffRequestDate> timeOffRequestDates = new ArrayList<>();

    Mockito.when(timeOffRequestDateRepository.saveAll(Mockito.any()))
        .thenReturn(timeOffRequestDates);

    Assertions.assertDoesNotThrow(
        () -> timeOffRequestDateService.saveAllTimeOffRequestDates(timeOffRequestDates));
  }
}
