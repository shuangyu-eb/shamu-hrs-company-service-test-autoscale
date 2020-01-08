package shamu.company.company.entity.mapper;

import org.mapstruct.Mapper;

import shamu.company.common.mapper.Config;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.entity.CompanyBenefitsSetting;

@Mapper(
    config = Config.class
)
public interface CompanyBenefitsSettingMapper {

  CompanyBenefitsSettingDto convertCompanyBenefitsSettingDto(
      CompanyBenefitsSetting companyBenefitsSetting);
}
