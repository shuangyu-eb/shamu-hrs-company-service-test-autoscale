package shamu.company.attendance.entity.mapper;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.attendance.dto.AllTimeDto;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface EmployeeTimeLogMapper {

  @Mapping(target = "logId", source = "employeeTimeLog.id")
  @Mapping(target = "duration", source = "employeeTimeLog.durationMin")
  @Mapping(target = "timeType", source = "employeeTimeLog.timeType.name")
  @Mapping(target = "startTime", expression = "java(formatStartTime(employeeTimeLog.getStart()))")
  @Mapping(target = "overtimeDetails", source = "overtimeDetailDto")
  AllTimeDto convertToTimeLogDto(
      EmployeeTimeLog employeeTimeLog, OvertimeDetailDto overtimeDetailDto);

  default String formatStartTime(final Timestamp startTime) {
    final SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    return dateFormat.format(new Date(startTime.getTime()));
  }

  @Mapping(target = "breakStart", source = "start")
  @Mapping(target = "breakMin", source = "durationMin")
  @Mapping(target = "timeType", source = "timeType.name")
  BreakTimeLogDto convertToBreakTimeLogDto(EmployeeTimeLog employeeTimeLog);
}
