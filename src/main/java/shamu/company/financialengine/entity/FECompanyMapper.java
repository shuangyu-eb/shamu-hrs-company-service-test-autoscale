package shamu.company.financialengine.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.financialengine.dto.NewFECompanyInfomation;
import shamu.company.financialengine.dto.NewCompanyDto;
import shamu.company.financialengine.dto.NewFinancialEngineAddressDto;
import shamu.company.financialengine.entity.FEAddresses.FeAddressType;

@Mapper(config = Config.class)
public interface FECompanyMapper {

  NewCompanyDto convertNewCompanyDto(NewFECompanyInfomation companyDetailsDto);

  @Mapping(target = "companyId", source = "feCompanyId")
  @Mapping(target = "street1", source = "officeAddress.street1")
  @Mapping(target = "street2", source = "officeAddress.street2")
  @Mapping(target = "city", source = "officeAddress.city")
  @Mapping(target = "statesProvince", source = "officeAddress.stateProvince.name")
  @Mapping(target = "postalCode", source = "officeAddress.postalCode")
  @Mapping(target = "country", source = "officeAddress.stateProvince.country.name")
  NewFinancialEngineAddressDto convertFinancialEngineAddressDto(
      String feCompanyId, OfficeAddress officeAddress, FeAddressType addressType);
}
