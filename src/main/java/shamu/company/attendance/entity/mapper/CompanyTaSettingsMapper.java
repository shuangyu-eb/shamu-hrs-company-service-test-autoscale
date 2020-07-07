package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface CompanyTaSettingsMapper {

  CompanyTaSettingsDto convertToCompanyTaSettingsDto(CompanyTaSetting companyTaSetting);

}
