package shamu.company.user.entity.mapper;

import io.micrometer.core.instrument.util.StringUtils;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.EmployeePersonalInformationDto;
import shamu.company.employee.dto.UserPersonalInformationForManagerDto;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.dto.MyEmployeePersonalInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.Ethnicity;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;

@Mapper(config = Config.class)
public interface UserPersonalInformationMapper {

  BasicUserPersonalInformationDto convertToBasicUserPersonalInformationDto(
      UserPersonalInformation userPersonalInformation);

  @Mapping(target = "gender", source = "genderId")
  @Mapping(target = "ethnicity", source = "ethnicityId")
  @Mapping(target = "maritalStatus", source = "maritalStatusId")
  void updateFromUserPersonalInformationDto(
      @MappingTarget UserPersonalInformation userPersonalInformation,
      UserPersonalInformationDto userPersonalInformationDto);

  @Mapping(target = "gender", source = "genderId")
  @Mapping(target = "ethnicity", source = "ethnicityId")
  @Mapping(target = "maritalStatus", source = "maritalStatusId")
  UserPersonalInformation createFromUserPersonalInformationDto(
      UserPersonalInformationDto userPersonalInformationDto);

  @Mapping(target = "genderId", source = "gender.id")
  @Mapping(target = "genderName", source = "gender.name")
  @Mapping(target = "ethnicityId", source = "ethnicity.id")
  @Mapping(target = "ethnicityName", source = "ethnicity.name")
  @Mapping(target = "maritalStatusId", source = "maritalStatus.id")
  @Mapping(target = "maritalStatusName", source = "maritalStatus.name")
  UserPersonalInformationDto convertToUserPersonalInformationDto(
      UserPersonalInformation userPersonalInformation);

  @Mapping(target = "genderId", source = "userPersonalInformation.gender.id")
  @Mapping(target = "genderName", source = "userPersonalInformation.gender.name")
  @Mapping(target = "ethnicityId", source = "userPersonalInformation.ethnicity.id")
  @Mapping(target = "ethnicityName", source = "userPersonalInformation.ethnicity.name")
  @Mapping(target = "maritalStatusId", source = "userPersonalInformation.maritalStatus.id")
  @Mapping(target = "maritalStatusName", source = "userPersonalInformation.maritalStatus.name")
  @Mapping(target = "imageUrl", source = "imgUrl")
  UserPersonalInformationDto convertToUserPersonalInformationDto(
      UserPersonalInformation userPersonalInformation, String imgUrl);

  @Mapping(target = "genderId", source = "gender.id")
  @Mapping(target = "genderName", source = "gender.name")
  @Mapping(target = "ethnicityId", source = "ethnicity.id")
  @Mapping(target = "ethnicityName", source = "ethnicity.name")
  @Mapping(target = "maritalStatusId", source = "maritalStatus.id")
  @Mapping(target = "maritalStatusName", source = "maritalStatus.name")
  MyEmployeePersonalInformationDto convertToMyEmployeePersonalInformationDto(
          UserPersonalInformation userPersonalInformation);

  @Mapping(target = "maritalStatusId", source = "maritalStatus.id")
  @Mapping(target = "maritalStatusName", source = "maritalStatus.name")
  UserPersonalInformationForManagerDto
      convertToUserPersonalInformationForManagerDto(
          UserPersonalInformation userPersonalInformation);

  @Mapping(target = "maritalStatusId", source = "maritalStatus.id")
  @Mapping(target = "maritalStatusName", source = "maritalStatus.name")
  EmployeePersonalInformationDto convertToEmployeePersonalInformationDto(
      UserPersonalInformation userPersonalInformation);

  default Gender convertFromGenderId(final String genderId) {
    return StringUtils.isEmpty(genderId) ? null : new Gender(genderId);
  }

  default Ethnicity convertFromEthnicityId(final String ethnicityId) {
    return StringUtils.isEmpty(ethnicityId) ? null : new Ethnicity(ethnicityId);
  }

  default MaritalStatus convertFromMaritalStatusId(final String maritalStatusId) {
    return StringUtils.isEmpty(maritalStatusId)  ? null : new MaritalStatus(maritalStatusId);
  }

  default Date convertFromString(final String birthDate) throws ParseException {
    if (StringUtils.isNotBlank(birthDate)) {
      DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
      return new Date(format.parse(birthDate).getTime());
    }
    return null;
  }
}
