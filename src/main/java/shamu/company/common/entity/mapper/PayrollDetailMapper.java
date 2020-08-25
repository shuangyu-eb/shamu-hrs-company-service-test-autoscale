package shamu.company.common.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface PayrollDetailMapper {

  @Mapping(target = "payFrequencyType.id", source = "payPeriodFrequencyId")
  @Mapping(target = "lastPayrollPayday", ignore = true)
  void updateFromCompanyTaSettingsDto(
      @MappingTarget PayrollDetail payrollDetail,
      CompanyTaSettingsDto companyTaSettingsDto,
      String payPeriodFrequencyId);
}
