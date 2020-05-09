package shamu.company.benefit.entity.mapper;

import org.apache.commons.lang.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.benefit.dto.BenefitDependentCreateDto;
import shamu.company.benefit.dto.BenefitDependentDto;
import shamu.company.benefit.dto.BenefitPlanDependentUserDto;
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.user.entity.Gender;

@Mapper(config = Config.class, uses = SelectFieldInformationDto.class)
public interface BenefitPlanDependentMapper {

  @Mapping(target = "employeeId", source = "employee.id")
  @Mapping(target = "phone", source = "phoneHome")
  @Mapping(target = "relationShip", source = "dependentRelationship")
  BenefitDependentDto convertToBenefitDependentDto(BenefitPlanDependent benefitPlanDependent);

  @Mapping(target = "imageUrl", source = "employee.imageUrl")
  BenefitPlanUserDto convertToBenefitPlanUserDto(BenefitPlanDependent benefitPlanDependent);

  BenefitPlanDependentUserDto convertToBenefitPlanDependentUser(
      BenefitPlanDependent benefitPlanDependent);

  BenefitPlanDependent createFromBenefitDependentCreateDto(
      BenefitDependentCreateDto benefitDependentCreateDto);

  void updateFromBenefitDependentCreateDto(
      @MappingTarget BenefitPlanDependent benefitPlanDependent,
      BenefitDependentCreateDto benefitDependentCreateDto);

  default Gender getGender(final Gender gender) {
    if (StringUtils.isEmpty(gender.getId())) {
      return null;
    } else {
      return gender;
    }
  }

  default StateProvince getStateProvince(final StateProvince state) {
    if (StringUtils.isEmpty(state.getId())) {
      return null;
    } else {
      return state;
    }
  }

  default DependentRelationship getDependentRelationship(
      final DependentRelationship dependentRelationship) {
    if (StringUtils.isEmpty(dependentRelationship.getId())) {
      return null;
    } else {
      return dependentRelationship;
    }
  }
}
