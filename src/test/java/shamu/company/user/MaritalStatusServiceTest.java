package shamu.company.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.service.MaritalStatusService;

import java.util.Optional;

public class MaritalStatusServiceTest {
  @Mock
  private MaritalStatusRepository maritalStatusRepository;

  @InjectMocks
  private MaritalStatusService maritalStatusService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenNotFound_thenShouldThrow() {
    Mockito.when(maritalStatusRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    Assertions.assertThrows(ResourceNotFoundException.class, () -> maritalStatusService.findById("test"));
  }

  @Test
  void whenFindAll_thenShouldCall() {
    maritalStatusService.findAll();
    Mockito.verify(maritalStatusRepository, Mockito.times(1)).findAll();
  }
}
