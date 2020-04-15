package shamu.company.common.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.StateProvinceRepository;

import java.util.ArrayList;
import java.util.Optional;

public class StateProvinceServiceTests {

  @Mock private StateProvinceRepository stateProvinceRepository;

  private StateProvinceService stateProvinceService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    stateProvinceService = new StateProvinceService(stateProvinceRepository);
  }

  @Nested
  class testFindById {
    @Test
    void whenIdExists_thenShouldSuccess() {
      final Optional<StateProvince> optional = Optional.of(new StateProvince());
      Mockito.when(stateProvinceRepository.findById(Mockito.anyString())).thenReturn(optional);
      Assertions.assertDoesNotThrow(() -> stateProvinceService.findById("1"));
    }

    @Test
    void whenIdNotExists_thenShouldThrowException() {
      final Optional<StateProvince> optional = Optional.empty();
      Mockito.when(stateProvinceRepository.findById(Mockito.anyString())).thenReturn(optional);
      Assertions.assertThrows(
          ResourceNotFoundException.class, () -> stateProvinceService.findById("1"));
    }
  }

  @Test
  void testFindAllByCountry() {
    Mockito.when(stateProvinceRepository.findAllByCountry(Mockito.any())).thenReturn(new ArrayList<>());
    Assertions.assertDoesNotThrow(() -> stateProvinceService.findAllByCountry("1"));
  }
}
