package shamu.company.employee.Repository;

import shamu.company.common.BaseRepository;
import shamu.company.employee.entity.CompensationFrequency;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "compensation_frequency")
public interface CompensationFrequencyRepository extends BaseRepository<CompensationFrequency, Long> {
}
