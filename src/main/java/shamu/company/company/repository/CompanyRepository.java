package shamu.company.company.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;

public interface CompanyRepository extends BaseRepository<Company, Long> {

  Boolean existsByName(String companyName);
}
