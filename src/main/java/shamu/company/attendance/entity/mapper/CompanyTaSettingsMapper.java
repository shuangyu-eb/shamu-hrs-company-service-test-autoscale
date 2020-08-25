package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface CompanyTaSettingsMapper {

  CompanyTaSettingsDto convertToCompanyTaSettingsDto(CompanyTaSetting companyTaSetting);

  void updateFromCompanyTaSettingsDto(
      @MappingTarget CompanyTaSetting companyTaSetting, CompanyTaSettingsDto companyTaSettingsDto);

  @Mapping(target = "timeZone", source = "staticTimezone")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateFromCompanyTaSettings(
      @MappingTarget CompanyTaSetting companyTaSetting,
      StaticTimezone staticTimezone,
      int messagingOn,
      int approvalDaysBeforePayroll,
      String startOfWeek);
}
