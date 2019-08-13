package shamu.company.company.entity.mapper;

import org.mapstruct.Mapper;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.mapper.Config;
import shamu.company.common.mapper.SelectFieldInformationDtoUtils;
import shamu.company.company.dto.StateProvinceDto;

@Mapper(
    config = Config.class,
    uses = SelectFieldInformationDtoUtils.class
)
public interface StateProvinceMapper {

  StateProvinceDto convertToStateProvinceDto(StateProvince stateProvince);
}
