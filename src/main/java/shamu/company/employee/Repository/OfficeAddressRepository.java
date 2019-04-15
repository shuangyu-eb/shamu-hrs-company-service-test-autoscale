package shamu.company.employee.Repository;

import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.company.entity.OfficeAddress;

import javax.persistence.Table;

@Repository
@Table(name = "office_addresses")
public interface OfficeAddressRepository extends BaseRepository<OfficeAddress, Long> {
}
