package shamu.company.company.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.common.mapper.Config;
import shamu.company.company.dto.OfficeAddressDto;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.OfficeAddress;

@Mapper(config = Config.class, uses = StateProvinceMapper.class)
public interface OfficeAddressMapper {

  @Mapping(target = "stateId", source = "stateProvince.id")
  @Mapping(target = "stateName", source = "stateProvince.name")
  @Mapping(target = "countryId", source = "stateProvince.country.id")
  @Mapping(target = "countryName", source = "stateProvince.country.name")
  OfficeAddressDto convertToOfficeAddressDto(OfficeAddress officeAddress);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "stateProvince", source = "officeCreateDto.stateId")
  @Mapping(target = "timeZone", source = "staticTimezone")
  OfficeAddress updateFromOfficeCreateDto(
      @MappingTarget OfficeAddress officeAddress, OfficeCreateDto officeCreateDto, StaticTimezone staticTimezone);
}
