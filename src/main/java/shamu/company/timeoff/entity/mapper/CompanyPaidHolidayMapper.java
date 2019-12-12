package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.user.entity.User;

@Mapper(config = Config.class)
public interface CompanyPaidHolidayMapper {

  @Mapping(target = "id", source = "companyPaidHoliday.paidHoliday.id")
  @Mapping(target = "name", source = "companyPaidHoliday.paidHoliday.name")
  @Mapping(target = "nameShow", source = "companyPaidHoliday.paidHoliday.nameShow")
  @Mapping(target = "date", source = "companyPaidHoliday.paidHoliday.date")
  @Mapping(target = "federal", source = "companyPaidHoliday.paidHoliday.federal")
  @Mapping(target = "editable", expression = "java(getEditable(companyPaidHoliday, user))")
  PaidHolidayDto convertToPaidHolidayDto(CompanyPaidHoliday companyPaidHoliday, AuthUser user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "paidHoliday", source = "paidHoliday")
  CompanyPaidHoliday createFromPaidHolidayDtoAndPaidHoliday(PaidHolidayDto paidHolidayDto,
      PaidHoliday paidHoliday);


  default boolean getEditable(final CompanyPaidHoliday companyPaidHoliday, final AuthUser user) {
    final User creator = companyPaidHoliday.getPaidHoliday().getCreator();

    if (creator != null) {
      return user.getId().equals(creator.getId());
    }
    return false;
  }
}
