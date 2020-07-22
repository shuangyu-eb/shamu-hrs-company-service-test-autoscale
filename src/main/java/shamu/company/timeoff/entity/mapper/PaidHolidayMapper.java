package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.server.dto.AuthUser;
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
      expression =
          "java(paidHolidayDto.getNameShow() != null "
              + "? paidHolidayDto.getNameShow() "
              + ": paidHolidayDto.getName())")
  @Mapping(target = "federal", constant = "false")
  PaidHoliday createFromPaidHolidayDtoAndCreator(PaidHolidayDto paidHolidayDto, User creator);

  @Mapping(target = "id", source = "paidHoliday.id")
  @Mapping(target = "name", source = "paidHoliday.name")
  @Mapping(target = "nameShow", source = "paidHoliday.nameShow")
  @Mapping(target = "date", source = "paidHoliday.date")
  @Mapping(target = "federal", source = "paidHoliday.federal")
  @Mapping(target = "editable", expression = "java(getEditable(paidHoliday, user))")
  PaidHolidayDto convertToPaidHolidayDto(PaidHoliday paidHoliday, AuthUser user);

  default boolean getEditable(final PaidHoliday paidHoliday, final AuthUser user) {
    final User creator = paidHoliday.getCreator();

    if (creator != null) {
      return user.getId().equals(creator.getId());
    }
    return false;
  }
}
