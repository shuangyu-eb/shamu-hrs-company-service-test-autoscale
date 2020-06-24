package shamu.company.attendance.entity.mapper;

import java.sql.Timestamp;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.attendance.dto.MyHoursEntryDto;
import shamu.company.attendance.dto.MyHoursListDto;
import shamu.company.attendance.dto.PayDetailDto;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface EmployeeTimeLogMapper {

  @Mapping(target = "timeRange", source = "timeRange")
  @Mapping(target = "minutes", source = "minutes")
  @Mapping(target = "pay", source = "pay")
  @Mapping(target = "timeType", source = "timeType")
  PayDetailDto convertToPayDetailDto(String timeRange, String minutes, String pay, String timeType);

  @Mapping(target = "date", source = "date")
  @Mapping(target = "comments", source = "comments")
  @Mapping(target = "payDetailDtos", source = "payDetailDtos")
  MyHoursEntryDto convertToMyHoursEntryDto(
      Timestamp date, String comments, List<PayDetailDto> payDetailDtos);

  @Mapping(target = "date", source = "date")
  @Mapping(target = "myHoursEntryDtos", source = "myHoursEntryDtos")
  MyHoursListDto convertToMyHoursListDto(String date, List<MyHoursEntryDto> myHoursEntryDtos);
}