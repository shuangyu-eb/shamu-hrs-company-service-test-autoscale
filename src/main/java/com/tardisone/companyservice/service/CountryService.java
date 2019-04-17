package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.Country;

public interface CountryService {
    Country findCountryByName(String name);
}