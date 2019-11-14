package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;
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

  default BenefitPlanType convertFromBenefitPlanTypeId(final String benefitPlanTypeId) {

    return !StringUtils.isEmpty(benefitPlanTypeId) ? new BenefitPlanType(benefitPlanTypeId) : null;
  }
}
