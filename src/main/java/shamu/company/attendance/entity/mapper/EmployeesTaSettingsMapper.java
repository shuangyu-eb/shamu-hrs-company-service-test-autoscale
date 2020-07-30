package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface EmployeesTaSettingsMapper {
  EmployeesTaSettingDto covertToEmployeesTaSettingsDto(EmployeesTaSetting employeesTaSetting);

  void updateFromEmployeeTaSettingsDto(
      @MappingTarget EmployeesTaSetting employeesTaSetting,
      EmployeesTaSettingDto employeesTaSettingDto);

  @Mapping(target = "timeZone", source = "staticTimezone")
  @Mapping(target = "employee.id", source = "employeeId")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  EmployeesTaSetting convertToEmployeeTaSettings(
      StaticTimezone staticTimezone, String employeeId, int messagingOn);
}
