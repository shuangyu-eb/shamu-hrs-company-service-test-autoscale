package shamu.company.company.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.entity.Tenant;
import shamu.company.common.mapper.Config;
import shamu.company.company.entity.Company;

@Mapper(config = Config.class)
public interface CompanyMapper {

  @Mapping(target = "companyId", source = "id")
  Tenant convertToTenant(Company company);

  Company convertToCompany(Tenant tenant);
}
