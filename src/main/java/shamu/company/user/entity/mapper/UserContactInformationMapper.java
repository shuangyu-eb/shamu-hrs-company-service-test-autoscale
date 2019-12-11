package shamu.company.user.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.EmployeeContactInformationDto;
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.entity.UserContactInformation;

@Mapper(config = Config.class)
public interface UserContactInformationMapper {

  UserContactInformationDto convertToUserContactInformationDto(
      UserContactInformation userContactInformation);

  EmployeeContactInformationDto convertToEmployeeContactInformationDto(
      UserContactInformation userContactInformation);

  BasicUserContactInformationDto convertToBasicUserContactInformationDto(
      UserContactInformation userContactInformation);

  @Mapping(target = "emailHome", source = "emailHome", defaultValue = "")
  UserContactInformation updateFromUserContactInformationDto(
      @MappingTarget UserContactInformation userContactInformation,
      UserContactInformationDto userContactInformationDto);

  UserContactInformation createFromUserContactInformationDto(
      UserContactInformationDto userContactInformationDto);
}
