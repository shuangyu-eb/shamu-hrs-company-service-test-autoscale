package shamu.company.common.repository;

import java.util.List;
import shamu.company.common.entity.Country;

public interface CountryRepository extends BaseRepository<Country, String> {

  Country findByName(String name);

  @Override
  List<Country> findAll();
}
