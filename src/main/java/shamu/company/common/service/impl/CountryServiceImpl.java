package shamu.company.common.service.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.Country;
import shamu.company.common.exception.ResouceNotFoundException;
import shamu.company.common.repository.CountryRepository;
import shamu.company.common.service.CountryService;

@Service
public class CountryServiceImpl implements CountryService {

  @Autowired
  CountryRepository countryRepository;

  @Override
  public Country getCountry(String name) {
    Optional<Country> optionalCountry = countryRepository.findCountryByName(name);
    return optionalCountry.orElseThrow(() -> new ResouceNotFoundException("Country does not exist"));
  }
}
