package shamu.company.employee.Repository;

import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.user.entity.MaritalStatus;

import javax.persistence.Table;

@Repository
@Table(name = "martial_status")
public interface MartialStatusRepository extends BaseRepository<MaritalStatus, Long> {
}
