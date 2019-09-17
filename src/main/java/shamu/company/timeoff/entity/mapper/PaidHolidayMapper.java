package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.entity.PaidHoliday;

@Mapper(config = Config.class)
public interface PaidHolidayMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "name", source = "paidHolidayDto.name")
  @Mapping(target = "company", source = "company")
  @Mapping(
      target = "nameShow",
      expression = "java(paidHolidayDto.getNameShow() != null "
          + "? paidHolidayDto.getNameShow() "
          + ": paidHolidayDto.getName())"
  )
  @Mapping(target = "federal", constant = "false")
  PaidHoliday createFromPaidHolidayDtoAndCompany(PaidHolidayDto paidHolidayDto, Company company);
}
