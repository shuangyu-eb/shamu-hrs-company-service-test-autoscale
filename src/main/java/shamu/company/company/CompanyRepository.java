package shamu.company.company;

import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;

public interface CompanyRepository extends BaseRepository<Company, Long> {

  Boolean existsByName(String companyName);

  Boolean existsBySubdomainName(String subDomainName);
}
