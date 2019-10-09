package shamu.company.common.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.Country;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.CountryRepository;

@Service
public class CountryService  {

  CountryRepository countryRepository;

  public CountryService(final CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }

  public Country getCountryById(final Long id) {
    final Optional<Country> optionalCountry = countryRepository.findById(id);
    return optionalCountry
        .orElseThrow(() -> new ResourceNotFoundException("Country does not exist"));
  }
}
