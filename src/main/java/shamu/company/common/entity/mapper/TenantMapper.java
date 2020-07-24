package shamu.company.common.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.admin.dto.TenantDto;
import shamu.company.common.entity.Tenant;
import shamu.company.common.mapper.Config;
import shamu.company.server.dto.CompanyDto;

@Mapper(config = Config.class)
public interface TenantMapper {

  @Mapping(target = "id", source = "companyId")
  TenantDto convertToTenantDto(Tenant tenant);

  List<TenantDto> convertToTenantDtos(List<Tenant> tenants);

  @Mapping(target = "id", source = "companyId")
  CompanyDto convertToCompanyDto(Tenant tenant);
}
