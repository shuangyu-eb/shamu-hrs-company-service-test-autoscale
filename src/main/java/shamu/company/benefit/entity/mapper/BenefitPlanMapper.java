package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface BenefitPlanMapper {

  BenefitPlanDto convertToBenefitPlanDto(BenefitPlan benefitPlane);

  @Mapping(target = "name", source = "planName")
  @Mapping(target = "benefitPlanType", source = "benefitPlanTypeId")
  @Mapping(target = "website", source = "planWebSite")
  BenefitPlan createFromBenefitPlanCreateDto(BenefitPlanCreateDto benefitPlanCreateDto);

  default BenefitPlanType getBenefitPlanType(final Long id) {
    return new BenefitPlanType(id);
  }
}
