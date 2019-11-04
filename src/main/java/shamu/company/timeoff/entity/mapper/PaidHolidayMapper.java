package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.user.entity.User;

@Mapper(config = Config.class)
public interface PaidHolidayMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "name", source = "paidHolidayDto.name")
  @Mapping(target = "creator", source = "creator")
  @Mapping(
      target = "nameShow",
      expression = "java(paidHolidayDto.getNameShow() != null "
          + "? paidHolidayDto.getNameShow() "
          + ": paidHolidayDto.getName())"
  )
  @Mapping(target = "federal", constant = "false")
  PaidHoliday createFromPaidHolidayDtoAndCreator(PaidHolidayDto paidHolidayDto, User creator);
}
