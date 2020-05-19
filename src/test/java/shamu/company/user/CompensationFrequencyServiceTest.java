package shamu.company.user;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.OldResourceNotFoundException;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.service.CompensationFrequencyService;

public class CompensationFrequencyServiceTest {
  @Mock private CompensationFrequencyRepository compensationFrequencyRepository;

  @InjectMocks private CompensationFrequencyService compensationFrequencyService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenNotFound_thenShouldThrow() {
    Mockito.when(compensationFrequencyRepository.findById(Mockito.anyString()))
        .thenReturn(Optional.empty());
    Assertions.assertThrows(
        OldResourceNotFoundException.class, () -> compensationFrequencyService.findById("test"));
  }

  @Test
  void whenFindAll_thenShouldCall() {
    compensationFrequencyService.findAll();
    Mockito.verify(compensationFrequencyRepository, Mockito.times(1)).findAll();
  }
}
