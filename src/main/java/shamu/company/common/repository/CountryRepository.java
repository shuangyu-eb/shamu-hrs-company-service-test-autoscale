package shamu.company.common.repository;


import shamu.company.common.entity.Country;

import java.util.Optional;

public interface CountryRepository extends BaseRepository<Country, Long>{
    Optional<Country> findCountryByName(String name);
}
