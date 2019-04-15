package shamu.company.employee.repository;

import javax.persistence.Table;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.user.entity.UserCompensation;

@Repository
@Table(name = "user_compensations")
public interface UserCompensationRepository extends BaseRepository<UserCompensation, Long> {

}
