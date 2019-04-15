package shamu.company.employee.repository;

import javax.persistence.Table;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.employee.entity.CompensationFrequency;

@Repository
@Table(name = "compensation_frequency")
public interface CompensationFrequencyRepository extends
    BaseRepository<CompensationFrequency, Long> {

}
