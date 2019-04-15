package shamu.company.employee.repository;

import javax.persistence.Table;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.user.entity.Gender;

@Repository
@Table(name = "genders")
public interface GenderRepository extends BaseRepository<Gender, Long> {

}
