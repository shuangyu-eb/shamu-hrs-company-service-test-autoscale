package shamu.company.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;

class StaticTimesheetStatusServiceTests {

  @InjectMocks private StaticTimesheetStatusService staticTimesheetStatusService;

  @Mock private StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void findByName() {
    final String status = TimeSheetStatus.APPROVED.name();
    final StaticTimesheetStatus staticTimesheetStatus = new StaticTimesheetStatus();
    staticTimesheetStatus.setName(status);

    Mockito.when(staticTimesheetStatusRepository.findByName(status))
        .thenReturn(staticTimesheetStatus);
    assertThat(staticTimesheetStatusService.findByName(status).getName()).isEqualTo(status);
    assertThatCode(() -> staticTimesheetStatusService.findByName(status))
        .doesNotThrowAnyException();
  }
}
