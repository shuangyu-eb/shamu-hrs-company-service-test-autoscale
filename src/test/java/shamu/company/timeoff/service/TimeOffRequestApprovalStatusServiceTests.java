package shamu.company.timeoff.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.repository.TimeOffRequestApprovalStatusRepository;

class TimeOffRequestApprovalStatusServiceTests {
  private static TimeOffRequestApprovalStatusService timeOffRequestApprovalStatusService;

  @Mock private TimeOffRequestApprovalStatusRepository timeOffRequestApprovalStatusRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    timeOffRequestApprovalStatusService =
        new TimeOffRequestApprovalStatusService(timeOffRequestApprovalStatusRepository);
  }

  @Test
  void findByName() {
    final TimeOffRequestApprovalStatus timeOffRequestApprovalStatus =
        new TimeOffRequestApprovalStatus();
    timeOffRequestApprovalStatus.setName("007");

    Mockito.when(timeOffRequestApprovalStatusRepository.findByName(Mockito.any()))
        .thenReturn(timeOffRequestApprovalStatus);

    Assertions.assertEquals(
        timeOffRequestApprovalStatusService.findByName(Mockito.any()).getName(),
        timeOffRequestApprovalStatus.getName());
    Assertions.assertDoesNotThrow(() -> timeOffRequestApprovalStatusService.findByName("007"));
  }
}
