package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.Country;
import com.tardisone.companyservice.repository.CountryRepository;
import com.tardisone.companyservice.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CountryServiceImpl implements CountryService {
    @Autowired
    CountryRepository repository;

    @Override
    public Country findCountryByName(String name) {
        return repository.findCountryByName(name);
    }
}
