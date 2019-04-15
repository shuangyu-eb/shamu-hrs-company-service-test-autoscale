package shamu.company.employee.repository;

import javax.persistence.Table;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.company.entity.Office;

@Repository
@Table(name = "offices")
public interface OfficeRepository extends BaseRepository<Office, Long> {

}
