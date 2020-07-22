package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.attendance.dto.StaticTimezoneDto;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface StaticTimeZonesMapper {
  StaticTimezoneDto covertToStaticTimeZonesDto(StaticTimezone staticTimezone);
}
