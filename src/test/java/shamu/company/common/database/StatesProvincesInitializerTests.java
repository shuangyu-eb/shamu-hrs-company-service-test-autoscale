package shamu.company.common.database;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.entity.Country;
import shamu.company.common.repository.CountryRepository;
import shamu.company.common.repository.StateProvinceRepository;

public class StatesProvincesInitializerTests {

  @Mock private StateProvinceRepository stateProvinceRepository;
  @Mock private CountryRepository countryRepository;
  private StatesProvincesInitializer statesProvincesInitializer;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    statesProvincesInitializer =
        new StatesProvincesInitializer(stateProvinceRepository, countryRepository);
  }

  @Test
  void whenListNull_thenShouldSuccess() {
    assertThatCode(() -> statesProvincesInitializer.run()).doesNotThrowAnyException();
  }

  @Test
  void whenListNotNull_thenShouldSuccess() {
    final Country country = new Country();
    final List<String> cities = new ArrayList<>();
    cities.add("1");
    cities.add("2");
    Mockito.when(countryRepository.findByName(Mockito.anyString())).thenReturn(country);
    Mockito.when(stateProvinceRepository.findAllNameByCountry(country)).thenReturn(cities);
    assertThatCode(() -> statesProvincesInitializer.run()).doesNotThrowAnyException();
  }
}
