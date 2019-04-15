package shamu.company.employee.Repository;

import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.common.entity.StateProvince;

import javax.persistence.Table;

@Repository
@Table(name = "states_provinces")
public interface StateProvinceRepository extends BaseRepository<StateProvince, Long> {
}
