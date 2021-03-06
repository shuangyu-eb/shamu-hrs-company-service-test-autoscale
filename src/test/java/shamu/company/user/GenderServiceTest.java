package shamu.company.user;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.service.GenderService;

public class GenderServiceTest {
  @Mock private GenderRepository genderRepository;

  @InjectMocks private GenderService genderService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenNotFound_thenShouldThrow() {
    Mockito.when(genderRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> genderService.findById("test"));
  }

  @Test
  void whenFindAll_thenShouldCall() {
    genderService.findAll();
    Mockito.verify(genderRepository, Mockito.times(1)).findAll();
  }
}
