package shamu.company.company.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.company.dto.OfficeAddressDto;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.OfficeAddress;

@Mapper(
    config = Config.class,
    uses = StateProvinceMapper.class
)
public interface OfficeAddressMapper {

  @Mapping(target = "zip", source = "postalCode")
  OfficeAddressDto convertToOfficeAddressDto(OfficeAddress officeAddress);

  @Mapping(target = "stateProvince", source = "stateId")
  @Mapping(target = "street1", source = "street1")
  @Mapping(target = "street2", source = "street2")
  @Mapping(target = "city", source = "city")
  @Mapping(target = "postalCode", source = "zip")
  OfficeAddress updateFromOfficeCreateDto(
          @MappingTarget OfficeAddress officeAddress, OfficeCreateDto officeCreateDto);
}
