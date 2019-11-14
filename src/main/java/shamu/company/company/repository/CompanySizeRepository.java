package shamu.company.company.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.CompanySize;

public interface CompanySizeRepository extends BaseRepository<CompanySize, String> {

  CompanySize findCompanySizeByName(String companySize);

  List<CompanySize> findAll();
}
