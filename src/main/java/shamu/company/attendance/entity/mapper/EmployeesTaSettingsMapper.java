package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface EmployeesTaSettingsMapper {
  EmployeesTaSettingDto covertToEmployeesTaSettingsDto(EmployeesTaSetting employeesTaSetting);
}
