package shamu.company.company.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.dto.OfficeDto;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;

@Mapper(
    config = Config.class,
    uses = OfficeAddressMapper.class
)
public interface OfficeMapper {

  OfficeDto convertToOfficeDto(Office office);

  List<OfficeDto> convertToOfficeDto(List<Office> office);

  @Mapping(target = "name", source = "officeCreateDto.officeName")
  @Mapping(target = "officeAddress", source = "officeAddress")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", expression = "java(shamu.company.utils.DateUtil.getToday())")
  Office convertToOffice(
          @MappingTarget Office office,
          OfficeCreateDto officeCreateDto,
          OfficeAddress officeAddress);
}
