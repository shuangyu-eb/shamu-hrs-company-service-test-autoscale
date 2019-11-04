package shamu.company.benefit.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface BenefitPlanCoverageMapper {

  @Mapping(target = "name", source = "benefitPlanCoverageDto.coverageName")
  @Mapping(target = "benefitPlan", source = "benefitPlan")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  BenefitPlanCoverage createFromBenefitPlanCoverageAndBenefitPlan(
      BenefitPlanCoverageDto benefitPlanCoverageDto, BenefitPlan benefitPlan);
}
