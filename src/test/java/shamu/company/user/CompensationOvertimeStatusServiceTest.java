package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;
import shamu.company.user.service.CompensationOvertimeStatusService;

public class CompensationOvertimeStatusServiceTest {
  @Mock
  private CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  @InjectMocks
  private CompensationOvertimeStatusService compensationOvertimeStatusService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenFindAll_thenShouldCall() {
    compensationOvertimeStatusService.findAll();
    Mockito.verify(compensationOvertimeStatusRepository, Mockito.times(1)).findAll();
  }
}
