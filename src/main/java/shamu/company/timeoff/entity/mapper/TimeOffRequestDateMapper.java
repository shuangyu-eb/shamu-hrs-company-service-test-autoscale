package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.TimeOffRequestDateDto;
import shamu.company.timeoff.entity.TimeOffRequestDate;

@Mapper(config = Config.class)
public interface TimeOffRequestDateMapper {

  TimeOffRequestDateDto convertToTimeOffRequestDateDto(TimeOffRequestDate timeOffRequestDate);
}
