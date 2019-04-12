package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.Country;
import com.tardisone.companyservice.exception.ForbiddenException;
import com.tardisone.companyservice.repository.CountryRepository;
import com.tardisone.companyservice.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CountryServiceImpl implements CountryService {
    @Autowired
    CountryRepository countryRepository;

    @Override
    public Country getCountry(String name) {
        Optional<Country> optionalCountry = countryRepository.findCountryByName(name);
        return optionalCountry.orElseThrow(() -> new ForbiddenException("Country does not exist"));
    }
}
