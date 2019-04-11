package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Country;

import java.util.Optional;

public interface CountryRepository extends BaseRepository<Country, Long>{
    Optional<Country> findCountryByName(String name);
}
