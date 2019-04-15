package shamu.company.employee.repository;

import javax.persistence.Table;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.user.entity.MaritalStatus;

@Repository
@Table(name = "martial_status")
public interface MartialStatusRepository extends BaseRepository<MaritalStatus, Long> {

}
