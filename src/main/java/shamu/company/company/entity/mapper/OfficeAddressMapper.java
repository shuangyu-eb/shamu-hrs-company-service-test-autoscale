package shamu.company.company.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.company.dto.OfficeAddressDto;
import shamu.company.company.entity.OfficeAddress;

@Mapper(
    config = Config.class,
    uses = StateProvinceMapper.class
)
public interface OfficeAddressMapper {

  @Mapping(target = "zip", source = "postalCode")
  OfficeAddressDto convertToOfficeAddressDto(OfficeAddress officeAddress);
}
