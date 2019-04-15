package shamu.company.employee.Repository;

import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.user.entity.UserCompensation;

import javax.persistence.Table;

@Repository
@Table(name = "user_compensations")
public interface UserCompensationRepository extends BaseRepository<UserCompensation, Long> {
}
