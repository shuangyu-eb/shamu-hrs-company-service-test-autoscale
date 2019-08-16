package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;

@Mapper(config = Config.class)
public interface CompanyPaidHolidayMapper {

  @Mapping(target = "id", source = "paidHoliday.id")
  @Mapping(target = "name", source = "paidHoliday.name")
  @Mapping(target = "nameShow", source = "paidHoliday.nameShow")
  @Mapping(target = "date", source = "paidHoliday.date")
  PaidHolidayDto convertToPaidHolidayDto(CompanyPaidHoliday companyPaidHoliday);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "paidHoliday", source = "paidHoliday")
  CompanyPaidHoliday createFromPaidHolidayDtoAndPaidHoliday(PaidHolidayDto paidHolidayDto,
      PaidHoliday paidHoliday);
}
