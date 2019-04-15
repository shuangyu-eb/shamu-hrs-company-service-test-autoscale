package shamu.company.employee.repository;

import javax.persistence.Table;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.common.entity.StateProvince;

@Repository
@Table(name = "states_provinces")
public interface StateProvinceRepository extends BaseRepository<StateProvince, Long> {

}
