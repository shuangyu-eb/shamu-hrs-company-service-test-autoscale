package shamu.company.common.database;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.repository.CountryRepository;
import shamu.company.common.repository.StateProvinceRepository;

@Component
public class StatesProvincesInitializer implements CommandLineRunner {

  private final StateProvinceRepository stateProvinceRepository;

  private final CountryRepository countryRepository;

  @Autowired
  public StatesProvincesInitializer(final StateProvinceRepository stateProvinceRepository,
      final CountryRepository countryRepository) {
    this.stateProvinceRepository = stateProvinceRepository;
    this.countryRepository = countryRepository;
  }

  @Override
  public void run(final String... args) {

    final Yaml yaml = new Yaml(new Constructor(CountryList.class));
    final InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream("db/application/states-provinces.yml");
    final CountryList countryList = yaml.load(inputStream);

    countryList.getCountries().forEach((countryItem -> {
      final Country country = countryRepository.findByName(countryItem.getName());
      final List<String> cities = stateProvinceRepository.findAllNameByCountry(country);

      final List<StateProvince> stateProvinces = countryItem.getCities().stream()
          .filter(cityName -> !cities.contains(cityName))
          .map(cityName -> StateProvince.builder()
              .country(country)
              .name(cityName)
              .build()).collect(Collectors.toList());

      if (!CollectionUtils.isEmpty(stateProvinces)) {
        stateProvinceRepository.saveAll(stateProvinces);
      }
    }));
  }
}
