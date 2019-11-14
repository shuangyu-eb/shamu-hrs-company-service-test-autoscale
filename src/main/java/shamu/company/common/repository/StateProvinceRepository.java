package shamu.company.common.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;

public interface StateProvinceRepository extends BaseRepository<StateProvince, String> {

  @Query(value = "select s.name from StateProvince s where s.country = ?1")
  List<String> findAllNameByCountry(Country country);

  List<StateProvince> findAllByCountry(Country country);
}
