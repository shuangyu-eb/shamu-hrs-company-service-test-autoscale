package shamu.company.company.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.CompanySize;

public interface CompanySizeRepository extends BaseRepository<CompanySize, Long> {

  CompanySize findCompanySizeByName(String companySize);
}
