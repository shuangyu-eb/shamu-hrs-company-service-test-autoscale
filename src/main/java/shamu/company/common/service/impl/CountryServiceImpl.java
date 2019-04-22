package shamu.company.common.service.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.Country;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.CountryRepository;
import shamu.company.common.service.CountryService;

@Service
public class CountryServiceImpl implements CountryService {

  @Autowired
  CountryRepository countryRepository;

  @Override
  public Country getCountryById(Long id) {
    Optional<Country> optionalCountry = countryRepository.findById(id);
    return optionalCountry
        .orElseThrow(() -> new ResourceNotFoundException("Country does not exist"));
  }
}
