package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.benefit.dto.RetirementPlanTypeDto;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface RetirementPlanTypeMapper {

  @Mapping(target = "retirementTypeId", source = "retirementPlanType.id")
  RetirementPlanTypeDto convertToRetirementPlanTypeDto(RetirementPlanType retirementPlanType);

  void updateFromNewRetirementPlanType(@MappingTarget RetirementPlanType retirementPlanType,
                                       RetirementPlanType newRetirementPlanType);
}
