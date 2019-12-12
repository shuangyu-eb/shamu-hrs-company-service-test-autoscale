package shamu.company.common.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.service.CountryService;

@RestApiController
public class CountryRestController {

  private final CountryService countryService;

  @Autowired
  public CountryRestController(final CountryService countryService) {
    this.countryService = countryService;
  }

  @GetMapping("countries")
  public List<CommonDictionaryDto> findCountries() {
    return countryService.findCountries();
  }
}
