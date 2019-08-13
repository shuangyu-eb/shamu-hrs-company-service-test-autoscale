package shamu.company.company.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import shamu.company.common.mapper.Config;
import shamu.company.company.dto.OfficeDto;
import shamu.company.company.entity.Office;

@Mapper(
    config = Config.class,
    uses = OfficeAddressMapper.class
)
public interface OfficeMapper {

  OfficeDto convertToOfficeDto(Office office);

  List<OfficeDto> convertToOfficeDto(List<Office> office);
}
