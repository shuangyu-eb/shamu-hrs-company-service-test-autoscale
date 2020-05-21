package shamu.company.common.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.entity.Country;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.CountryRepository;

public class CountryServiceTests {

  @Mock private CountryRepository countryRepository;

  private CountryService countryService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    countryService = new CountryService(countryRepository);
  }

  @Test
  void testFindCountries() {
    Mockito.when(countryRepository.findAll()).thenReturn(new ArrayList<>());
    Assertions.assertDoesNotThrow(() -> countryService.findCountries());
  }

  @Nested
  class testFindById {

    @Test
    void whenIdExists_thenShouldSuccess() {
      final Optional<Country> country = Optional.of(new Country());
      Mockito.when(countryRepository.findById(Mockito.anyString())).thenReturn(country);
      assertThatCode(() -> countryService.findById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrowException() {
      final Optional<Country> country = Optional.empty();
      Mockito.when(countryRepository.findById(Mockito.anyString())).thenReturn(country);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> countryService.findById("1"));
    }
  }
}
