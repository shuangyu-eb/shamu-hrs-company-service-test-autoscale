package shamu.company.attendance.entity.mapper;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.attendance.dto.MyHoursEntryDto;
import shamu.company.attendance.dto.MyHoursTimeLogDto;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface EmployeeTimeLogMapper {

  @Mapping(target = "logId", source = "employeeTimeLog.id")
  @Mapping(target = "duration", source = "employeeTimeLog.durationMin")
  @Mapping(target = "timeType", source = "employeeTimeLog.timeType.name")
  @Mapping(target = "basePay", source = "basePay")
  @Mapping(target = "startTime", expression = "java(formatStartTime(employeeTimeLog.getStart()))")
  MyHoursTimeLogDto convertToMyHoursTimeLogDto(EmployeeTimeLog employeeTimeLog, String basePay);

  @Mapping(target = "entryId", source = "employeeTimeLog.entry.id")
  @Mapping(target = "comments", source = "employeeTimeLog.entry.comment")
  @Mapping(target = "myHoursTimeLogDtos", source = "myHoursTimeLogDtos")
  MyHoursEntryDto convertToMyHoursEntryDto(
      EmployeeTimeLog employeeTimeLog, List<MyHoursTimeLogDto> myHoursTimeLogDtos);

  default String formatStartTime(final Timestamp startTime) {
    final SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    return dateFormat.format(new Date(startTime.getTime()));
  }
}
