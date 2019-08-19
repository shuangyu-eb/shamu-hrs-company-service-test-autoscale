package shamu.company.company.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.mapper.Config;
import shamu.company.company.dto.StateProvinceDto;
import shamu.company.employee.dto.SelectFieldInformationDto;

@Mapper(
    config = Config.class,
    uses = SelectFieldInformationDto.class
)
public interface StateProvinceMapper {

  StateProvinceDto convertToStateProvinceDto(StateProvince stateProvince);
}
