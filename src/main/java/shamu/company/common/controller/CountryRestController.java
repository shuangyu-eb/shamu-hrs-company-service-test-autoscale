package shamu.company.common.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.entity.Country;
import shamu.company.common.repository.CountryRepository;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class CountryRestController {

  private final CountryRepository countryRepository;

  @Autowired
  public CountryRestController(final CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }

  @GetMapping("countries")
  public List<CommonDictionaryDto> getCountries() {
    List<Country> countries = countryRepository.findAll();
    return ReflectionUtil.convertTo(countries, CommonDictionaryDto.class);
  }
}
