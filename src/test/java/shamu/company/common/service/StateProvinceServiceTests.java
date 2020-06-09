package shamu.company.common.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.repository.StateProvinceRepository;

public class StateProvinceServiceTests {

  @Mock private StateProvinceRepository stateProvinceRepository;

  private StateProvinceService stateProvinceService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    stateProvinceService = new StateProvinceService(stateProvinceRepository);
  }

  @Test
  void testFindAllByCountry() {
    Mockito.when(stateProvinceRepository.findAllByCountry(Mockito.any()))
        .thenReturn(new ArrayList<>());
    assertThatCode(() -> stateProvinceService.findAllByCountry("1")).doesNotThrowAnyException();
  }

  @Nested
  class testFindById {
    @Test
    void whenIdExists_thenShouldSuccess() {
      final Optional<StateProvince> optional = Optional.of(new StateProvince());
      Mockito.when(stateProvinceRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatCode(() -> stateProvinceService.findById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrowException() {
      final Optional<StateProvince> optional = Optional.empty();
      Mockito.when(stateProvinceRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> stateProvinceService.findById("1"));
    }
  }
}
