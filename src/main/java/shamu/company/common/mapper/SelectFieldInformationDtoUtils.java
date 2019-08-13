package shamu.company.common.mapper;

import org.mapstruct.Mapper;
import shamu.company.employee.dto.SelectFieldInformationDto;

@Mapper(config = Config.class)
public interface SelectFieldInformationDtoUtils {

  SelectFieldInformationDto convertToDto(Object origin);

  SelectFieldInformationDto convertToDto(String id, String name);
}
