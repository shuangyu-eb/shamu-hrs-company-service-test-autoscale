package shamu.company.company;

import shamu.company.company.entity.Company;
import shamu.company.common.BaseRepository;

public interface CompanyRepository extends BaseRepository<Company, Long> {
    Boolean existsByName(String companyName);

    Boolean existsBySubdomainName(String subDomainName);
}
