package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Country;

public interface CountryRepository extends BaseRepository<Country, Long>{
    Country findCountryByName(String name);
}
