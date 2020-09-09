package shamu.company.company.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;
import shamu.company.server.dto.CompanyDtoProjection;

public interface CompanyRepository extends BaseRepository<Company, String> {

  Boolean existsByName(String companyName);

  Company findCompanyById(String companyId);

  @Query(
      value =
          "select hex(c.id) as id, c.name as name from companies c "
              + "right join users u on c.id = u.company_id where u.id = unhex(?1)",
      nativeQuery = true)
  CompanyDtoProjection findCompanyDtoByUserId(String id);
}
