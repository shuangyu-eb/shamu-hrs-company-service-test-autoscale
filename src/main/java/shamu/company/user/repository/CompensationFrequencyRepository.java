package shamu.company.user.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;
import shamu.company.job.entity.CompensationFrequency;

public interface CompensationFrequencyRepository extends
    BaseRepository<CompensationFrequency, Long> {

  List<CompensationFrequency> findAllByCompany(Company company);

}
