package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.repository.DeactivationReasonRepository;
import shamu.company.user.service.DeactivationReasonService;


public class DeactivationReasonServiceTest {
  @Mock
  private DeactivationReasonRepository deactivationReasonRepository;

  @InjectMocks
  private DeactivationReasonService deactivationReasonService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenFindAll_thenShouldCall() {
    deactivationReasonService.findAll();
    Mockito.verify(deactivationReasonRepository, Mockito.times(1)).findAll();
  }
}
