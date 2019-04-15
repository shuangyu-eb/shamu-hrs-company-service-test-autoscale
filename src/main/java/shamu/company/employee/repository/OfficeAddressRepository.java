package shamu.company.employee.repository;

import javax.persistence.Table;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.company.entity.OfficeAddress;

@Repository
@Table(name = "office_addresses")
public interface OfficeAddressRepository extends BaseRepository<OfficeAddress, Long> {

}
