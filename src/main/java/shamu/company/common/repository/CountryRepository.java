package shamu.company.common.repository;

import java.util.Optional;
import shamu.company.common.entity.Country;

public interface CountryRepository extends BaseRepository<Country, Long> {

  Optional<Country> findCountryByName(String name);
}
