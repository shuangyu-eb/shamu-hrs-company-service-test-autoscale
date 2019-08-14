package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.benefit.dto.BenefitDependentCreateDto;
import shamu.company.benefit.dto.BenefitDependentDto;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.mapper.Config;
import shamu.company.common.mapper.SelectFieldInformationDtoUtils;
import shamu.company.user.entity.Gender;

@Mapper(
    config = Config.class,
    uses = SelectFieldInformationDtoUtils.class
)
public interface BenefitPlanDependentMapper {

  @Mapping(target = "employeeId", source = "employee.id")
  @Mapping(target = "phone", source = "phoneHome")
  @Mapping(target = "relationShip", source = "dependentRelationship")
  BenefitDependentDto convertToBenefitDependentDto(BenefitPlanDependent benefitPlanDependent);

  BenefitPlanDependent createFromBenefitDependentCreateDto(
      BenefitDependentCreateDto benefitDependentCreateDto);

  void updateFromBenefitDependentCreateDto(@MappingTarget BenefitPlanDependent benefitPlanDependent,
      BenefitDependentCreateDto benefitDependentCreateDto);

  default Gender getGender(final Gender gender) {
    if (null == gender.getId()) {
      return null;
    } else {
      return gender;
    }
  }

  default StateProvince getStateProvince(final StateProvince state) {
    if (null == state.getId()) {
      return null;
    } else {
      return state;
    }
  }

  default DependentRelationship getDependentRelationship(
      final DependentRelationship dependentRelationship) {
    if (null == dependentRelationship.getId()) {
      return null;
    } else {
      return dependentRelationship;
    }
  }
}
